package by.nexus.core.service.local;

import by.nexus.core.exception.FailedToCreateStorageFolderException;
import by.nexus.core.exception.FailedToMoveFileException;
import by.nexus.core.exception.FileNodeNotExistsException;
import by.nexus.core.exception.PermissionException;
import by.nexus.core.exception.ProjectNotExistsException;
import by.nexus.core.model.entity.FileNode;
import by.nexus.core.model.entity.FileVersion;
import by.nexus.core.model.entity.Project;
import by.nexus.core.model.entity.ProjectPermission;
import by.nexus.core.repository.FileNodeRepository;
import by.nexus.core.repository.FileVersionRepository;
import by.nexus.core.repository.ProjectPermissionRepository;
import by.nexus.core.repository.ProjectRepository;
import by.nexus.core.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LocalStorageFileSystemService implements FileSystemService {

    @Value("${nexus.storage.path}")
    private String storagePath;

    private final FileNodeRepository fileNodeRepository;
    private final FileVersionRepository fileVersionRepository;
    private final ProjectRepository projectRepository;
    private final ProjectPermissionRepository projectPermissionRepository;

    @Override
    public Project createProject(String userId, String projectName, String description) {
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }

        if (projectRepository.existsByNameAndOwnerId(projectName, userId)) {
            throw new IllegalArgumentException("Project with name " + projectName + " already exists");
        }

        Project project = Project.builder()
                .name(projectName)
                .ownerId(userId)
                .description(description)
                .build();
        project = projectRepository.save(project);

        FileNode root = FileNode.builder()
                .name("root")
                .type(FileNode.FileType.FOLDER)
                .project(project)
                .build();

        ProjectPermission ownerPermission = ProjectPermission.builder()
                .id(new ProjectPermission.ProjectPermissionId(project.getId(), userId))
                .accessLevel(ProjectPermission.AccessLevel.OWNER)
                .build();
        projectPermissionRepository.save(ownerPermission);

        root = fileNodeRepository.save(root);
        String path = generateStoragePath(userId, project.getId());
        FileVersion version = FileVersion.builder()
                .fileNode(root)
                .storagePath(path)
                .sizeBytes(0L)
                .build();
        version = fileVersionRepository.save(version);

        root.setCurrentVersion(version);
        fileNodeRepository.save(root);

        if (!initStorageFolder(path)) {
            throw new FailedToCreateStorageFolderException("Failed to create storage folder: " + path);
        }

        return project;
    }

    private boolean initStorageFolder(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            return true;
        } catch (IOException e) {
            log.error("Failed to create storage folder: {}", path, e);
            return false;
        }
    }

    private String generateStoragePath(String userId, UUID projectId) {
        return Paths.get(storagePath, userId, projectId.toString()).toString();
    }

    @Override
    public void deleteProject(String userId, String projectName) {
        projectRepository.findByNameAndOwnerId(projectName, userId)
                .ifPresent(project -> deleteProject(project, userId));
    }

    @Override
    public void deleteProject(String userId, UUID projectId) {
        projectRepository.findById(projectId)
                .ifPresent(project -> deleteProject(project, userId));
    }

    private void deleteProject(Project project, String userId) {
        checkPermission(userId, project.getId(), ProjectPermission.AccessLevel.ADMIN);
        projectRepository.delete(project);
        String rootPath = generateStoragePath(userId, project.getId());
        deleteDirectoryRecursively(Paths.get(rootPath));
    }

    @Override
    public FileNode createNode(UUID projectId, UUID parentId, String name, FileNode.FileType type, String userId) {
        FileNode parent = getFileNode(parentId);

        checkFileNodeName(parent, name);
        checkPermission(userId, projectId, ProjectPermission.AccessLevel.EDITOR);

        FileNode node = FileNode.builder()
                .name(name)
                .type(type)
                .project(projectRepository.findById(projectId).orElseThrow(
                        () -> new ProjectNotExistsException(
                                "Project with id " + projectId + " not exists"
                        )
                ))
                .parent(parent)
                .build();
        node = fileNodeRepository.save(node);

        Path parentPath = Paths.get(parent.getCurrentVersion().getStoragePath());
        Path newNodePath = parentPath.resolve(name);

        try {
            if (type == FileNode.FileType.FOLDER) {
                Files.createDirectories(newNodePath);
            } else {
                Files.createFile(newNodePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create physical file", e);
        }

        FileVersion version = FileVersion.builder()
                .fileNode(node)
                .version(1)
                .storagePath(newNodePath.toString())
                .sizeBytes(0L)
                .createdBy(userId)
                .build();
        version = fileVersionRepository.save(version);

        node.setCurrentVersion(version);
        return fileNodeRepository.save(node);
    }

    @Override
    public void deleteNode(UUID nodeId, String userId) {
        Project project = getFileNode(nodeId).getProject();
        checkPermission(userId, project.getId(), ProjectPermission.AccessLevel.EDITOR);

        fileNodeRepository.findById(nodeId)
                .ifPresent(node -> {
                            node.setDeleted(true);
                            fileNodeRepository.save(node);
                        }
                );
    }

    private void deleteDirectoryRecursively(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            log.error("Failed to delete project folder: {}", path, e);
        }
    }

    @Override
    public void moveNode(UUID nodeId, UUID targetParentId, String userId) {
        FileNode node = getFileNode(nodeId);
        Project project = node.getProject();
        checkPermission(userId, project.getId(), ProjectPermission.AccessLevel.EDITOR);

        FileNode targetParent = getFileNode(targetParentId);

        if (targetParent.getType() != FileNode.FileType.FOLDER) {
            throw new IllegalArgumentException("Target is not a folder");
        }

        Path sourcePath = Paths.get(node.getCurrentVersion().getStoragePath());
        Path targetPath = Paths.get(targetParent.getCurrentVersion().getStoragePath()).resolve(node.getName());

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE);

            node.setParent(targetParent);

            FileVersion currentVersion = node.getCurrentVersion();
            currentVersion.setStoragePath(targetPath.toString());
            fileVersionRepository.save(currentVersion);

            fileNodeRepository.save(node);
        } catch (IOException e) {
            throw new FailedToMoveFileException("Failed to move file on disk", e);
        }
    }

    private FileVersion incrementVersion(FileNode node) {
        FileVersion version = node.getCurrentVersion().increment();
        node.setCurrentVersion(version);
        return version;
    }

    private FileNode getFileNode(UUID nodeId) {
        return fileNodeRepository.findById(nodeId)
                .orElseThrow(() -> new FileNodeNotExistsException(
                        "FileNode with id " + nodeId + " not exists"
                ));
    }

    @Override
    public void renameNode(UUID nodeId, String newName, String userId) {
        FileNode node = getFileNode(nodeId);
        Project project = node.getProject();

        checkPermission(userId, project.getId(), ProjectPermission.AccessLevel.EDITOR);
        checkFileNodeName(node.getParent(), newName);

        Path oldPath = Paths.get(node.getCurrentVersion().getStoragePath());
        Path newPath = oldPath.resolveSibling(newName);

        try {
            Files.move(oldPath, newPath, StandardCopyOption.ATOMIC_MOVE);

            FileVersion currentVersion = node.getCurrentVersion();
            currentVersion.setStoragePath(newPath.toString());
            fileVersionRepository.save(currentVersion);

            node.setName(newName);
            fileNodeRepository.save(node);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rename file on disk", e);
        }
    }


    private void checkFileNodeName(FileNode parent, String name) {
        if (fileNodeRepository.existsByParentIdAndName(parent.getId(), name)) {
            throw new IllegalArgumentException("File with name '" + name + "' already exists");
        }
    }

    @Override
    public List<FileNode> listFiles(UUID projectId) {
        return fileNodeRepository.findAllByProjectSorted(projectId);
    }

    @Override
    public void commitVersion(UUID nodeId) {
        fileVersionRepository.save(
                incrementVersion(getFileNode(nodeId))
        );
    }

    @Override
    public List<FileVersion> getHistory(UUID nodeId) {
        return fileVersionRepository.findAllByFileNodeId(nodeId);
    }

    @Override
    public void rollbackToVersion(UUID nodeId, Integer version) {
        FileNode node = getFileNode(nodeId);
        FileVersion oldVersion = node.getCurrentVersion();
        FileVersion newVersion = fileVersionRepository.findAllByFileNodeId(nodeId).stream()
                .filter(v -> Objects.equals(v.getVersion(), version))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Version " + version + " does not exist for the node " + nodeId
                        )
                );
        newVersion.setVersion(oldVersion.getVersion() + 1);
        newVersion.setId(null);
        fileVersionRepository.save(newVersion);

        node.setCurrentVersion(newVersion);
        fileNodeRepository.save(node);
    }

    @Override
    public void rollbackToVersion(UUID nodeId, UUID versionId) {
        FileNode node = getFileNode(nodeId);
        FileVersion oldVersion = node.getCurrentVersion();
        FileVersion newVersion = fileVersionRepository.findById(versionId).orElseThrow(
                () -> new IllegalArgumentException(
                        "Version " + versionId + " does not exist"
                )
        );

        if (!newVersion.getFileNode().getId().equals(nodeId)) {
            throw new IllegalArgumentException(
                    "Version " + versionId + " does not exist for the node " + nodeId
            );
        }

        newVersion.setVersion(oldVersion.getVersion() + 1);
        newVersion.setId(null);
        fileVersionRepository.save(newVersion);

        node.setCurrentVersion(newVersion);
        fileNodeRepository.save(node);
    }

    @Override
    public void checkPermission(String userId, UUID projectId, ProjectPermission.AccessLevel level) {
        ProjectPermission permission = projectPermissionRepository.findById(
                        new ProjectPermission.ProjectPermissionId(
                                projectId, userId
                        )
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Permission for project " + projectId +
                                        " and user " + userId + " does not exist"
                        )
                );

        if (!permission.getAccessLevel().hasAccess(level)) {
            throw new PermissionException(
                    "You do not have permission to edit this project"
            );
        }
    }

    @Override
    public void shareProject(UUID projectId, String userId, ProjectPermission.AccessLevel level) {
        ProjectPermission.ProjectPermissionId id = new ProjectPermission.ProjectPermissionId(
                projectId, userId
        );

        Optional<ProjectPermission> permissionOptional = projectPermissionRepository.findById(id);
        ProjectPermission permission;
        if (permissionOptional.isPresent()) {
            permission = permissionOptional.get();
            permission.setAccessLevel(level);
        } else {
            permission = ProjectPermission.builder()
                    .id(id)
                    .project(projectRepository.findById(projectId).orElseThrow(
                            () -> new ProjectNotExistsException(
                                    "Project " + projectId + " does not exist"
                            )
                    ))
                    .accessLevel(level)
                    .build();
        }

        projectPermissionRepository.save(permission);
    }

    @Override
    public void updateVersionSize(UUID nodeId, long size, String userId) {
        FileNode node = getFileNode(nodeId);

        checkPermission(userId, node.getProject().getId(), ProjectPermission.AccessLevel.EDITOR);

        FileVersion version = node.getCurrentVersion();
        version.setSizeBytes(size);
        fileVersionRepository.save(version);
    }
}

package by.nexus.core.service.local;

import by.nexus.core.model.entity.FileNode;
import by.nexus.core.model.entity.FileVersion;
import by.nexus.core.model.entity.Project;
import by.nexus.core.model.entity.ProjectPermission;
import by.nexus.core.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalStorageFileSystemService implements FileSystemService {

    @Override
    public Project createProject(String userId, String projectName, String description) {
        return null;
    }

    @Override
    public void deleteProject(String userId, String projectName) {

    }

    @Override
    public FileNode createNode(Long projectId, Long parentId, String name, FileNode.FileType type) {
        return null;
    }

    @Override
    public void deleteNode(Long nodeId) {

    }

    @Override
    public void moveNode(Long nodeId, Long targetParentId) {

    }

    @Override
    public void renameNode(Long nodeId, String newName) {

    }

    @Override
    public TreeMap<Long, FileNode> listFiles(Long projectId) {
        return null;
    }

    @Override
    public TreeMap<Long, FileNode> listFiles(Long projectId, Long parentId) {
        return null;
    }

    @Override
    public void commitVersion(Long projectId, Long nodeId) {

    }

    @Override
    public List<FileVersion> getHistory(Long projectId, Long nodeId) {
        return List.of();
    }

    @Override
    public void rollbackToVersion(Long projectId, Long nodeId, Integer version) {

    }

    @Override
    public void rollbackToVersion(Long projectId, Long nodeId, Long versionId) {

    }

    @Override
    public boolean hasAccess(String userId, Long projectId, ProjectPermission.AccessLevel level) {
        return false;
    }

    @Override
    public boolean checkPermission(String userId, Long projectId, ProjectPermission.AccessLevel level) {
        return false;
    }

    @Override
    public void shareProject(Long projectId, String userId, ProjectPermission.AccessLevel level) {

    }
}

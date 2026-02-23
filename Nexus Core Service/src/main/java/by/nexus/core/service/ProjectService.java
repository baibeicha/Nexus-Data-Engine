package by.nexus.core.service;

import by.nexus.core.exception.ProjectNotExistsException;
import by.nexus.core.model.dto.api.CreateProjectRequest;
import by.nexus.core.model.dto.api.ProjectDto;
import by.nexus.core.model.dto.api.ShareProjectRequest;
import by.nexus.core.model.entity.Project;
import by.nexus.core.model.entity.ProjectPermission;
import by.nexus.core.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final FileSystemService fileSystemService;
    private final ProjectRepository projectRepository;

    public ProjectDto createProject(CreateProjectRequest request) {
        Project project = fileSystemService.createProject(
                request.ownerId(), request.name(), request.description()
        );

        return mapToDto(project);
    }


    public List<ProjectDto> getUserProjects(String userId) {
        return projectRepository.findAllByOwnerId(userId).parallelStream()
                .map(this::mapToDto)
                .toList();
    }

    public ProjectDto getProject(String userId, String projectId) {
        fileSystemService.checkPermission(
                userId,
                UUID.fromString(projectId),
                ProjectPermission.AccessLevel.VIEWER
        );

        Project project = projectRepository.findById(UUID.fromString(projectId))
                .orElseThrow(
                        () -> new ProjectNotExistsException(
                                "Project with id " + projectId + " does not exist"
                        )
                );

        return mapToDto(project);
    }

    private ProjectDto mapToDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public void delete(String userId, String projectId) {
        fileSystemService.deleteProject(userId, UUID.fromString(projectId));
    }

    public void share(String projectId, ShareProjectRequest request) {
        UUID pid = UUID.fromString(projectId);

        fileSystemService.checkPermission(
                request.userId(),
                pid,
                ProjectPermission.AccessLevel.ADMIN
        );

        fileSystemService.shareProject(
                pid,
                request.targetUserId(),
                ProjectPermission.AccessLevel.valueOf(request.accessLevel())
        );
    }
}

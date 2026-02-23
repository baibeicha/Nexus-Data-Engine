package by.nexus.core.controller.api.v1;

import by.nexus.core.model.dto.api.CreateProjectRequest;
import by.nexus.core.model.dto.api.ProjectDto;
import by.nexus.core.model.dto.api.ShareProjectRequest;
import by.nexus.core.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectDto createProject(@RequestBody CreateProjectRequest request) {
        return projectService.createProject(request);
    }

    @GetMapping
    public List<ProjectDto> getProjects(@RequestBody String userId) {
        return projectService.getUserProjects(userId);
    }

    @GetMapping("/{projectId}")
    public ProjectDto getProject(@RequestBody String userId, @PathVariable String projectId) {
        return projectService.getProject(userId, projectId);
    }

    @DeleteMapping("/{projectId}")
    public void deleteProject(@RequestBody String userId, @PathVariable String projectId) {
        projectService.delete(userId, projectId);
    }

    @PostMapping("/{projectId}/share")
    public void shareProject(@RequestBody ShareProjectRequest request, @PathVariable String projectId) {
        projectService.share(projectId, request);
    }
}

package by.nexus.core.controller.api.v1;

import by.nexus.core.model.dto.api.CreateProjectRequest;
import by.nexus.core.model.dto.api.ProjectDto;
import by.nexus.core.model.dto.api.ShareProjectRequest;
import by.nexus.core.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createProject_ShouldReturnCreatedProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        CreateProjectRequest request = new CreateProjectRequest(
                "user@example.com",
                "Test Project",
                "Test Description"
        );
        
        ProjectDto response = new ProjectDto(
                projectId,
                "Test Project",
                "Test Description",
                "user@example.com",
                Instant.now(),
                Instant.now()
        );
        
        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.ownerId").value("user@example.com"));
        
        verify(projectService).createProject(any(CreateProjectRequest.class));
    }

    @Test
    void getProjects_ShouldReturnListOfProjects() throws Exception {
        String userId = "user@example.com";
        List<ProjectDto> projects = List.of(
                new ProjectDto(UUID.randomUUID(), "Project 1", "Desc 1", userId, Instant.now(), Instant.now()),
                new ProjectDto(UUID.randomUUID(), "Project 2", "Desc 2", userId, Instant.now(), Instant.now())
        );
        
        when(projectService.getUserProjects(userId)).thenReturn(projects);
        
        mockMvc.perform(get("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + userId + "\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Project 1"))
                .andExpect(jsonPath("$[1].name").value("Project 2"));
        
        verify(projectService).getUserProjects(userId);
    }

    @Test
    void getProject_ShouldReturnProject() throws Exception {
        String userId = "user@example.com";
        String projectId = UUID.randomUUID().toString();
        
        ProjectDto project = new ProjectDto(
                UUID.fromString(projectId),
                "Test Project",
                "Test Description",
                userId,
                Instant.now(),
                Instant.now()
        );
        
        when(projectService.getProject(userId, projectId)).thenReturn(project);
        
        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + userId + "\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Test Project"));
        
        verify(projectService).getProject(userId, projectId);
    }

    @Test
    void deleteProject_ShouldReturnNoContent() throws Exception {
        String userId = "user@example.com";
        String projectId = UUID.randomUUID().toString();
        
        doNothing().when(projectService).delete(userId, projectId);
        
        mockMvc.perform(delete("/api/v1/projects/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + userId + "\""))
                .andExpect(status().isOk());
        
        verify(projectService).delete(userId, projectId);
    }

    @Test
    void shareProject_ShouldReturnOk() throws Exception {
        String projectId = UUID.randomUUID().toString();
        ShareProjectRequest request = new ShareProjectRequest(
                "owner@example.com",
                "user2@example.com",
                projectId,
                "EDITOR"
        );
        
        doNothing().when(projectService).share(eq(projectId), any(ShareProjectRequest.class));
        
        mockMvc.perform(post("/api/v1/projects/{projectId}/share", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(projectService).share(eq(projectId), any(ShareProjectRequest.class));
    }
}

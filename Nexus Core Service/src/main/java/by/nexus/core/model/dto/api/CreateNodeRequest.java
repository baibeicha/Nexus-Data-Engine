package by.nexus.core.model.dto.api;

public record CreateNodeRequest(
        String projectId,
        String parentId,
        String name,
        String type,
        String userId
) {
}

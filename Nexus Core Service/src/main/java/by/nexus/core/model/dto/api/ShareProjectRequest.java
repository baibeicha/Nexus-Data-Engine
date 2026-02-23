package by.nexus.core.model.dto.api;

public record ShareProjectRequest (
        String userId,
        String targetUserId,
        String projectId,
        String accessLevel
) {
}

package by.nexus.core.model.dto.api;

public record MoveNodeRequest(
        String targetParentId,
        String userId
) {
}

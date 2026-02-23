package by.nexus.core.model.dto.api;

public record RenameNodeRequest(
        String newName,
        String userId
) {
}

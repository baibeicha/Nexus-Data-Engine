package by.nexus.core.model.dto.api;

public record CreateProjectRequest (
        String ownerId,
        String name,
        String description
) {
}

package by.nexus.core.model.dto.api;

import java.time.Instant;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String description,
        String ownerId,
        Instant createdAt,
        Instant updatedAt
) {
}

package by.nexus.core.model.dto.api;

import java.time.Instant;
import java.util.UUID;

public record FileNodeDto(
        UUID id,
        String name,
        String type,
        UUID parentId,
        UUID projectId,
        UUID currentVersionId,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {
}

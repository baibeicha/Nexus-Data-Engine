package by.nexus.core.model.dto.api;

import java.time.Instant;
import java.util.UUID;

public record FileVersionDto(
        UUID id,
        Integer version,
        String storagePath,
        Long sizeBytes,
        String createdBy,
        Instant createdAt
) {
}

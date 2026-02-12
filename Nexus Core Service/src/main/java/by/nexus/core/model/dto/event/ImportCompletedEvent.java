package by.nexus.core.model.dto.event;

public record ImportCompletedEvent(
        String jobId,
        String status,          // SUCCESS|FAILED
        String storagePath,
        long fileSize,
        String errorMessage
) {
}


package by.nexus.data.processor.event;

public record ImportCompletedEvent(
        String jobId,
        String status,          // SUCCESS|FAILED
        String storagePath,
        long fileSize,
        String errorMessage
) {}


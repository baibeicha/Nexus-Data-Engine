package by.nexus.sync.model;

import java.time.Instant;
import java.util.Map;

public record SyncEvent(
        String type,
        String message,
        Map<String, Object> data,
        Instant timestamp
) {
    public SyncEvent {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public SyncEvent(String type, String message) {
        this(type, message, null, Instant.now());
    }

    public SyncEvent(String type, String message, Map<String, Object> data) {
        this(type, message, data, Instant.now());
    }

    public static SyncEvent jobCompleted(String fileId, String status) {
        return new SyncEvent(
                "JOB_COMPLETED",
                "Import job completed",
                Map.of("fileId", fileId, "status", status)
        );
    }

    public static SyncEvent jobFailed(String fileId, String errorMessage) {
        return new SyncEvent(
                "JOB_FAILED",
                "Import job failed",
                Map.of("fileId", fileId, "error", errorMessage)
        );
    }

    public static SyncEvent fileUpdated(String fileId, String updatedBy) {
        return new SyncEvent(
                "FILE_UPDATED",
                "File has been updated",
                Map.of("fileId", fileId, "updatedBy", updatedBy)
        );
    }
}

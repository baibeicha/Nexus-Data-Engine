package by.nexus.core.model.dto.event;

public record ImportRequestEvent (
        String jobId,
        String projectId,
        String connectionUrl,
        String username,
        String password,
        String sqlQuery
) {}


package by.nexus.core.model.dto.api;

public record SqlImportRequest(
        String userId,
        String projectId,
        String targetFolderId,
        ConnectionDto connection,
        String query
) {
    public record ConnectionDto(
            String url,
            String user,
            String password
    ) {
    }
}

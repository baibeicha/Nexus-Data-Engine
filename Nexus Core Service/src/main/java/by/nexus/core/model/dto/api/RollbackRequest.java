package by.nexus.core.model.dto.api;

public record RollbackRequest(
        Integer targetVersion,
        String versionId
) {
}

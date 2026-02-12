package by.nexus.core.service;

import by.nexus.core.model.dto.DatabaseConnectionDetails;
import by.nexus.core.model.dto.event.ImportCompletedEvent;

public interface ImportOrchestrator {
    long startImportJob(String userId, Long projectId, Long targetFolderId,
                        DatabaseConnectionDetails dbConnectionDetails, String sqlQuery);
    void handleSuccess(ImportCompletedEvent event);
    void handleFailure(ImportCompletedEvent event);
}

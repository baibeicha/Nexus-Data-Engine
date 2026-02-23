package by.nexus.core.service;

import by.nexus.core.model.dto.DatabaseConnectionDetails;
import by.nexus.core.model.dto.event.ImportCompletedEvent;

import java.util.UUID;

public interface ImportOrchestrator {
    String startImportJob(String userId, UUID projectId, UUID targetFolderId,
                        DatabaseConnectionDetails dbConnectionDetails, String sqlQuery);
    void handleSuccess(ImportCompletedEvent event);
    void handleFailure(ImportCompletedEvent event);
}

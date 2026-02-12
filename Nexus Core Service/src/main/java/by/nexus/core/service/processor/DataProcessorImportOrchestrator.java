package by.nexus.core.service.processor;

import by.nexus.core.model.dto.DatabaseConnectionDetails;
import by.nexus.core.model.dto.event.ImportCompletedEvent;
import by.nexus.core.service.ImportOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataProcessorImportOrchestrator implements ImportOrchestrator {
    
    @Override
    public long startImportJob(String userId, Long projectId, Long targetFolderId, DatabaseConnectionDetails dbConnectionDetails, String sqlQuery) {
        return 0;
    }

    @Override
    public void handleSuccess(ImportCompletedEvent event) {

    }

    @Override
    public void handleFailure(ImportCompletedEvent event) {

    }
}

package by.nexus.core.controller.api.v1;

import by.nexus.core.model.dto.DatabaseConnectionDetails;
import by.nexus.core.model.dto.api.SqlImportRequest;
import by.nexus.core.service.ImportOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportOrchestrator importOrchestrator;

    @PostMapping("/sql")
    public ResponseEntity<Map<String, String>> importFromSql(@RequestBody SqlImportRequest request) {
        String jobId = importOrchestrator.startImportJob(
                request.userId(),
                UUID.fromString(request.projectId()),
                request.targetFolderId() != null ? UUID.fromString(request.targetFolderId()) : null,
                new DatabaseConnectionDetails(
                        request.connection().url(),
                        request.connection().user(),
                        request.connection().password()
                ),
                request.query()
        );
        
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }
}

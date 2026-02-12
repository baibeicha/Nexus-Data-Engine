package by.nexus.core.service.processor;

import by.nexus.core.model.dto.DatabaseConnectionDetails;
import by.nexus.core.model.dto.event.ImportCompletedEvent;
import by.nexus.core.model.dto.event.ImportRequestEvent;
import by.nexus.core.model.entity.FileNode;
import by.nexus.core.service.FileSystemService;
import by.nexus.core.service.ImportOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessorImportOrchestrator implements ImportOrchestrator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FileSystemService fileSystemService;
    private final RestTemplate restTemplate;

    @Value("${spring.kafka.topics.import-request}")
    private String importRequestTopic;

    @Value("${spring.kafka.topics.sync-events}")
    private String syncEventsTopic;

    @Value("${nexus.storage.download-base-url}")
    private String baseUrl;

    @Override
    public String startImportJob(String userId, UUID projectId, UUID targetFolderId,
                               DatabaseConnectionDetails dbConnectionDetails, String sqlQuery) {
        String jobId = String.format("%s:%s:%s:%s", userId, projectId, targetFolderId, UUID.randomUUID());

        ImportRequestEvent event = new ImportRequestEvent(
                jobId,
                projectId.toString(),
                dbConnectionDetails.url(),
                dbConnectionDetails.user(),
                dbConnectionDetails.password(),
                sqlQuery
        );

        kafkaTemplate.send(importRequestTopic, event);
        return jobId;
    }

    @Override
    @Transactional
    public void handleSuccess(ImportCompletedEvent event) {
        log.info("Processing successful import for job: {}", event.jobId());

        try {
            String[] parts = event.jobId().split(":");
            if (parts.length < 4) {
                log.error("Invalid Job ID format: {}", event.jobId());
                return;
            }
            String userId = parts[0];
            UUID projectId = UUID.fromString(parts[1]);
            UUID folderId = UUID.fromString(parts[2]);
            String importName = "Imported_" + parts[3].substring(0, 8);

            Path tempFile = downloadFile(event.storagePath());

            FileNode node = fileSystemService.createNode(
                    projectId,
                    folderId,
                    importName + ".nxdt",
                    FileNode.FileType.DATASET,
                    userId
            );

            Path finalPath = Path.of(node.getCurrentVersion().getStoragePath());
            Files.move(tempFile, finalPath, StandardCopyOption.REPLACE_EXISTING);

            fileSystemService.updateVersionSize(node.getId(), Files.size(finalPath), userId);

            // kafkaTemplate.send(syncEventsTopic, new SyncEvent("JOB_SUCCESS", node.getId()));

            log.info("Successfully imported dataset: {}", finalPath);

        } catch (Exception e) {
            log.error("Failed to process import result", e);
        }
    }

    private Path downloadFile(String url) throws IOException {
        Path tempFile = Files.createTempFile("import_", ".nxdt");

        restTemplate.execute(URI.create(url), HttpMethod.GET, null, clientHttpResponse -> {
            try (InputStream is = clientHttpResponse.getBody()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        });

        return tempFile;
    }

    @Override
    public void handleFailure(ImportCompletedEvent event) {
        log.info("Import failed for job: {}, error: {}", event.jobId(), event.errorMessage());
        // kafkaTemplate.send(syncEventsTopic, new SyncEvent("JOB_FAILED", node.getId()));
    }
}

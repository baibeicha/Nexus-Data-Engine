package by.nexus.data.processor.service.impl;

import by.nexus.data.processor.event.ImportCompletedEvent;
import by.nexus.data.processor.event.ImportRequestEvent;
import by.nexus.data.processor.service.DataExtractionService;
import by.nexus.data.processor.service.ImportProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaImportProcessorService implements ImportProcessorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DataExtractionService extractionService;

    @Value("${spring.kafka.topics.import-completed}")
    private String importCompletedTopic;

    @Value("${nexus.storage.download-base-url}")
    private String baseUrl;

    @KafkaListener(
            topics = "${spring.kafka.topics.import-request}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Override
    public void handleImportRequest(ImportRequestEvent command) {
        try {
            File resultFile = extractionService.extractAndConvert(command);

            String downloadUrl = baseUrl + "/" + resultFile.getName();
            long fileSize = resultFile.length();

            ImportCompletedEvent successEvent = new ImportCompletedEvent(
                    command.jobId(),
                    "SUCCESS",
                    downloadUrl,
                    fileSize,
                    null
            );

            kafkaTemplate.send(importCompletedTopic, command.jobId(), successEvent);

        } catch (Exception e) {
            log.error("[JobId: {}] Failed.", command.jobId(), e);
            ImportCompletedEvent errorEvent = new ImportCompletedEvent(
                    command.jobId(),
                    "FAILED",
                    null,
                    0,
                    e.getMessage()
            );
            kafkaTemplate.send(importCompletedTopic, command.jobId(), errorEvent);
        }
    }
}


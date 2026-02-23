package by.nexus.sync.listener;

import by.nexus.sync.controller.WebSocketController;
import by.nexus.sync.model.SyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventListener {

    private final WebSocketController webSocketController;

    @KafkaListener(
            topics = "${spring.kafka.topics.import-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleImportCompleted(Map<String, Object> event) {
        log.info("Received import completed event: {}", event);
        
        String jobId = (String) event.get("jobId");
        String status = (String) event.get("status");
        String storagePath = (String) event.get("storagePath");
        
        // Extract projectId from jobId (format: userId:projectId:folderId:uuid)
        if (jobId != null) {
            String[] parts = jobId.split(":");
            if (parts.length >= 2) {
                String projectId = parts[1];
                
                SyncEvent syncEvent;
                if ("SUCCESS".equals(status)) {
                    syncEvent = SyncEvent.jobCompleted(jobId, status);
                } else {
                    String errorMessage = (String) event.get("errorMessage");
                    syncEvent = SyncEvent.jobFailed(jobId, errorMessage);
                }
                
                webSocketController.broadcastJobCompletion(projectId, syncEvent);
            }
        }
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.sync-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleSyncEvent(SyncEvent event) {
        log.info("Received sync event: {}", event);
        // Handle other sync events if needed
    }
}

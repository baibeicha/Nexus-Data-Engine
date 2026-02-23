package by.nexus.core.listener;

import by.nexus.core.model.dto.event.ImportCompletedEvent;
import by.nexus.core.service.ImportOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobCompletionListener {

    private final ImportOrchestrator importOrchestrator;

    @KafkaListener(
            topics = "${spring.kafka.topics.import-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenForJobCompletion(ImportCompletedEvent event) {
        switch (event.status()) {
            case "SUCCESS" -> importOrchestrator.handleSuccess(event);
            case "FAILED" -> importOrchestrator.handleFailure(event);
            default -> importOrchestrator.handleFailure(event);
        }
    }
}

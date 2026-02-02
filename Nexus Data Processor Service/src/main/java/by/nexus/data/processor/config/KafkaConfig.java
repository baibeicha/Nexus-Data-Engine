package by.nexus.data.processor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(
                (record, exception) -> log.error(
        "Не удалось обработать сообщение после всех попыток. Topic: {}, Partition: {}, Offset: {}, Value: {}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.value(),
                        exception
                ),
                new FixedBackOff(1000L, 2)
        );
    }
}

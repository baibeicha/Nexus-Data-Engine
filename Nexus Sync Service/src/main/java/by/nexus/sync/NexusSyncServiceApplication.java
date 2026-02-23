package by.nexus.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class NexusSyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusSyncServiceApplication.class, args);
    }

}

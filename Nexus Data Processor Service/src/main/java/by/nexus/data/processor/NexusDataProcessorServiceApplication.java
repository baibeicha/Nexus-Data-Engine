package by.nexus.data.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NexusDataProcessorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusDataProcessorServiceApplication.class, args);
    }

}

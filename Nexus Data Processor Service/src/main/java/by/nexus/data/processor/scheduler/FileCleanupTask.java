package by.nexus.data.processor.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileCleanupTask {

    @Value("${nexus.storage.path}")
    private String storagePath;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanOldFiles() {
        log.info("Starting storage cleanup...");

        Instant retentionLimit = Instant.now().minus(24, ChronoUnit.HOURS);

        try (Stream<java.nio.file.Path> files = Files.list(Paths.get(storagePath))) {
            files.forEach(path -> {
                try {
                    File file = path.toFile();
                    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                    Instant fileCreationTime = attr.creationTime().toInstant();

                    if (fileCreationTime.isBefore(retentionLimit)) {
                        if (file.delete()) {
                            log.info("Deleted old file: {}", file.getName());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error deleting file: {}", path, e);
                }
            });
        } catch (Exception e) {
            log.error("Storage cleanup failed", e);
        }
    }
}

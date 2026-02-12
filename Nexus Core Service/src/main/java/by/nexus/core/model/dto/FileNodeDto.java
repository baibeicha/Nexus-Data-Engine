package by.nexus.core.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FileNodeDto {
    private UUID id;
    private String name;
    private String type; // FOLDER, FILE
    private List<FileNodeDto> children;
    private Long size;
    private Instant updatedAt;
}


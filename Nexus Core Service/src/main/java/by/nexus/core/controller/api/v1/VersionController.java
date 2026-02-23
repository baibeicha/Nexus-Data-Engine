package by.nexus.core.controller.api.v1;

import by.nexus.core.model.dto.api.FileVersionDto;
import by.nexus.core.model.dto.api.RollbackRequest;
import by.nexus.core.model.entity.FileVersion;
import by.nexus.core.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionController {

    private final FileSystemService fileSystemService;

    @GetMapping("/file/{fileNodeId}")
    public ResponseEntity<List<FileVersionDto>> getFileHistory(@PathVariable String fileNodeId) {
        List<FileVersion> versions = fileSystemService.getHistory(UUID.fromString(fileNodeId));
        
        List<FileVersionDto> dtos = versions.stream()
                .map(this::mapToDto)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{fileNodeId}/rollback")
    public ResponseEntity<Void> rollbackToVersion(
            @PathVariable String fileNodeId,
            @RequestBody RollbackRequest request) {
        
        if (request.targetVersion() != null) {
            fileSystemService.rollbackToVersion(
                    UUID.fromString(fileNodeId), 
                    request.targetVersion()
            );
        } else if (request.versionId() != null) {
            fileSystemService.rollbackToVersion(
                    UUID.fromString(fileNodeId), 
                    UUID.fromString(request.versionId())
            );
        }
        
        return ResponseEntity.ok().build();
    }

    private FileVersionDto mapToDto(FileVersion version) {
        return new FileVersionDto(
                version.getId(),
                version.getVersion(),
                version.getStoragePath(),
                version.getSizeBytes(),
                version.getCreatedBy(),
                version.getCreatedAt()
        );
    }
}

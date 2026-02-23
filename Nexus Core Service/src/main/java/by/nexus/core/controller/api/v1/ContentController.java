package by.nexus.core.controller.api.v1;

import by.nexus.core.model.entity.FileNode;
import by.nexus.core.model.entity.FileVersion;
import by.nexus.core.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final FileSystemService fileSystemService;

    @GetMapping("/{fileNodeId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileNodeId,
            @RequestParam String userId) {
        
        try {
            FileNode node = getFileNode(UUID.fromString(fileNodeId), userId);
            FileVersion version = node.getCurrentVersion();
            
            if (version == null || version.getStoragePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            File file = new File(version.getStoragePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + node.getName() + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            log.error("Error downloading file: {}", fileNodeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{fileNodeId}/version/{version}")
    public ResponseEntity<Resource> downloadFileVersion(
            @PathVariable String fileNodeId,
            @PathVariable Integer version,
            @RequestParam String userId) {
        
        try {
            FileNode node = getFileNode(UUID.fromString(fileNodeId), userId);
            
            List<FileVersion> versions = fileSystemService.getHistory(UUID.fromString(fileNodeId));
            FileVersion targetVersion = versions.stream()
                    .filter(v -> v.getVersion().equals(version))
                    .findFirst()
                    .orElse(null);
            
            if (targetVersion == null || targetVersion.getStoragePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            File file = new File(targetVersion.getStoragePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + node.getName() + "_v" + version + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            log.error("Error downloading file version: {} v{}", fileNodeId, version, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam String projectId,
            @RequestParam String parentId,
            @RequestParam String userId) {
        
        try {
            FileNode node = fileSystemService.createNode(
                    UUID.fromString(projectId),
                    parentId != null ? UUID.fromString(parentId) : null,
                    file.getOriginalFilename(),
                    FileNode.FileType.DATASET,
                    userId
            );
            
            Path targetPath = Path.of(node.getCurrentVersion().getStoragePath());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            fileSystemService.updateVersionSize(node.getId(), file.getSize(), userId);
            
            return ResponseEntity.ok().build();
            
        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private FileNode getFileNode(UUID fileNodeId, String userId) {
        // This is a simplified version - in production, you'd want to check permissions
        // and return the actual FileNode
        return null; // Placeholder - needs proper implementation
    }
}

package by.nexus.core.controller.api.v1;

import by.nexus.core.model.dto.api.CreateNodeRequest;
import by.nexus.core.model.dto.api.FileNodeDto;
import by.nexus.core.model.dto.api.MoveNodeRequest;
import by.nexus.core.model.dto.api.RenameNodeRequest;
import by.nexus.core.model.entity.FileNode;
import by.nexus.core.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileSystemController {

    private final FileSystemService fileSystemService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FileNodeDto>> listFiles(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "tree") String view) {
        
        List<FileNode> files = fileSystemService.listFiles(UUID.fromString(projectId));
        
        List<FileNodeDto> dtos = files.stream()
                .map(this::mapToDto)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<FileNodeDto> createNode(@RequestBody CreateNodeRequest request) {
        FileNode node = fileSystemService.createNode(
                UUID.fromString(request.projectId()),
                request.parentId() != null ? UUID.fromString(request.parentId()) : null,
                request.name(),
                FileNode.FileType.valueOf(request.type()),
                request.userId()
        );
        
        return ResponseEntity.ok(mapToDto(node));
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<Void> moveNode(
            @PathVariable String id,
            @RequestBody MoveNodeRequest request) {
        
        fileSystemService.moveNode(
                UUID.fromString(id),
                UUID.fromString(request.targetParentId()),
                request.userId()
        );
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<Void> renameNode(
            @PathVariable String id,
            @RequestBody RenameNodeRequest request) {
        
        fileSystemService.renameNode(
                UUID.fromString(id),
                request.newName(),
                request.userId()
        );
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(
            @PathVariable String id,
            @RequestParam String userId) {
        
        fileSystemService.deleteNode(UUID.fromString(id), userId);
        
        return ResponseEntity.noContent().build();
    }

    private FileNodeDto mapToDto(FileNode node) {
        return new FileNodeDto(
                node.getId(),
                node.getName(),
                node.getType().name(),
                node.getParent() != null ? node.getParent().getId() : null,
                node.getProject().getId(),
                node.getCurrentVersion() != null ? node.getCurrentVersion().getId() : null,
                node.isDeleted(),
                node.getCreatedAt(),
                node.getUpdatedAt()
        );
    }
}

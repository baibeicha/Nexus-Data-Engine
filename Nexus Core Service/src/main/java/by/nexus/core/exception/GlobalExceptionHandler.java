package by.nexus.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProjectNotExistsException.class)
    public ResponseEntity<Map<String, Object>> handleProjectNotExists(ProjectNotExistsException ex) {
        log.error("Project not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(FileNodeNotExistsException.class)
    public ResponseEntity<Map<String, Object>> handleFileNodeNotExists(FileNodeNotExistsException ex) {
        log.error("File node not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<Map<String, Object>> handlePermissionException(PermissionException ex) {
        log.error("Permission denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(FailedToCreateStorageFolderException.class)
    public ResponseEntity<Map<String, Object>> handleFailedToCreateStorageFolder(FailedToCreateStorageFolderException ex) {
        log.error("Failed to create storage folder: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create storage folder"));
    }

    @ExceptionHandler(FailedToMoveFileException.class)
    public ResponseEntity<Map<String, Object>> handleFailedToMoveFile(FailedToMoveFileException ex) {
        log.error("Failed to move file: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to move file"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String message) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}

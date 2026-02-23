package by.nexus.sync.controller;

import by.nexus.sync.model.SyncEvent;
import by.nexus.sync.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    @SubscribeMapping("/topic/project/{projectId}")
    public void subscribeToProject(@DestinationVariable String projectId, Principal principal) {
        log.info("User {} subscribed to project {}", principal.getName(), projectId);
        roomService.joinRoom(projectId, principal.getName());
        
        // Notify others that user joined
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/presence",
                Map.of("user", principal.getName(), "action", "JOINED")
        );
    }

    @SubscribeMapping("/topic/file/{fileId}")
    public void subscribeToFile(@DestinationVariable String fileId, Principal principal) {
        log.info("User {} subscribed to file {}", principal.getName(), fileId);
        roomService.joinFileRoom(fileId, principal.getName());
        
        // Notify others that user is viewing the file
        messagingTemplate.convertAndSend(
                "/topic/file/" + fileId + "/presence",
                Map.of("user", principal.getName(), "action", "VIEWING")
        );
    }

    @MessageMapping("/file/{fileId}/lock")
    public void lockFile(@DestinationVariable String fileId, 
                         @Payload Map<String, String> payload,
                         Principal principal) {
        String userId = principal.getName();
        log.info("User {} requesting lock for file {}", userId, fileId);
        
        boolean locked = roomService.acquireLock(fileId, userId);
        
        if (locked) {
            messagingTemplate.convertAndSend(
                    "/topic/file/" + fileId + "/locks",
                    Map.of("user", userId, "action", "LOCKED")
            );
        } else {
            String currentLockHolder = roomService.getLockHolder(fileId);
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/file/" + fileId + "/locks",
                    Map.of("error", "File already locked by " + currentLockHolder)
            );
        }
    }

    @MessageMapping("/file/{fileId}/unlock")
    public void unlockFile(@DestinationVariable String fileId, Principal principal) {
        String userId = principal.getName();
        log.info("User {} unlocking file {}", userId, fileId);
        
        boolean unlocked = roomService.releaseLock(fileId, userId);
        
        if (unlocked) {
            messagingTemplate.convertAndSend(
                    "/topic/file/" + fileId + "/locks",
                    Map.of("user", userId, "action", "UNLOCKED")
            );
        }
    }

    @MessageMapping("/file/{fileId}/cursor")
    public void updateCursor(@DestinationVariable String fileId,
                             @Payload Map<String, Object> cursorData,
                             Principal principal) {
        String userId = principal.getName();
        cursorData.put("user", userId);
        
        messagingTemplate.convertAndSend(
                "/topic/file/" + fileId + "/cursors",
                cursorData
        );
    }

    @MessageMapping("/project/{projectId}/notify")
    public void sendNotification(@DestinationVariable String projectId,
                                  @Payload SyncEvent event) {
        log.info("Broadcasting notification to project {}: {}", projectId, event);
        
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/notifications",
                event
        );
    }

    public void broadcastJobCompletion(String projectId, SyncEvent event) {
        log.info("Broadcasting job completion to project {}: {}", projectId, event);
        
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/notifications",
                event
        );
    }

    public Set<String> getOnlineUsers(String projectId) {
        return roomService.getRoomUsers(projectId);
    }
}

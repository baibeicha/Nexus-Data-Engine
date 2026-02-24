package by.nexus.sync.listener;

import by.nexus.sync.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;

/**
 * Слушатель событий WebSocket.
 * Обрабатывает подключение, отключение и подписку пользователей.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket session connected: {} - User: {}", sessionId, username);
        
        // Notify others about user connection
        messagingTemplate.convertAndSend(
                "/topic/presence",
                Map.of("user", username, "action", "CONNECTED", "sessionId", sessionId)
        );
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket session disconnected: {} - User: {}", sessionId, username);
        
        // Clean up user from all rooms
        roomService.removeUserFromAllRooms(username);
        
        // Notify others about user disconnection
        messagingTemplate.convertAndSend(
                "/topic/presence",
                Map.of("user", username, "action", "DISCONNECTED", "sessionId", sessionId)
        );
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        log.info("User {} subscribed to {} (session: {})", username, destination, sessionId);
        
        // Extract project or file ID from destination
        if (destination != null) {
            if (destination.startsWith("/topic/project/")) {
                String[] parts = destination.split("/");
                if (parts.length >= 3) {
                    String projectId = parts[3];
                    roomService.joinRoom(projectId, username);
                }
            } else if (destination.startsWith("/topic/file/")) {
                String[] parts = destination.split("/");
                if (parts.length >= 3) {
                    String fileId = parts[3];
                    roomService.joinFileRoom(fileId, username);
                }
            }
        }
    }
}

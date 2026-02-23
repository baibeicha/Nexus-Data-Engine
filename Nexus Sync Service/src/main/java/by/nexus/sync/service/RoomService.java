package by.nexus.sync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
public class RoomService {

    // projectId -> set of userIds
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    
    // fileId -> set of userIds (viewing the file)
    private final Map<String, Set<String>> fileRooms = new ConcurrentHashMap<>();
    
    // fileId -> userId (lock holder)
    private final Map<String, String> fileLocks = new ConcurrentHashMap<>();

    public void joinRoom(String projectId, String userId) {
        rooms.computeIfAbsent(projectId, k -> new CopyOnWriteArraySet<>()).add(userId);
        log.info("User {} joined room {}", userId, projectId);
    }

    public void leaveRoom(String projectId, String userId) {
        Set<String> room = rooms.get(projectId);
        if (room != null) {
            room.remove(userId);
            if (room.isEmpty()) {
                rooms.remove(projectId);
            }
        }
        log.info("User {} left room {}", userId, projectId);
    }

    public Set<String> getRoomUsers(String projectId) {
        return rooms.getOrDefault(projectId, Set.of());
    }

    public void joinFileRoom(String fileId, String userId) {
        fileRooms.computeIfAbsent(fileId, k -> new CopyOnWriteArraySet<>()).add(userId);
        log.info("User {} joined file room {}", userId, fileId);
    }

    public void leaveFileRoom(String fileId, String userId) {
        Set<String> room = fileRooms.get(fileId);
        if (room != null) {
            room.remove(userId);
            if (room.isEmpty()) {
                fileRooms.remove(fileId);
            }
        }
        // Also release any lock held by this user
        releaseLock(fileId, userId);
        log.info("User {} left file room {}", userId, fileId);
    }

    public Set<String> getFileRoomUsers(String fileId) {
        return fileRooms.getOrDefault(fileId, Set.of());
    }

    public boolean acquireLock(String fileId, String userId) {
        // Check if already locked by someone else
        String currentHolder = fileLocks.get(fileId);
        if (currentHolder != null && !currentHolder.equals(userId)) {
            log.warn("File {} is already locked by {}", fileId, currentHolder);
            return false;
        }
        
        fileLocks.put(fileId, userId);
        log.info("User {} acquired lock on file {}", userId, fileId);
        return true;
    }

    public boolean releaseLock(String fileId, String userId) {
        String currentHolder = fileLocks.get(fileId);
        if (currentHolder != null && currentHolder.equals(userId)) {
            fileLocks.remove(fileId);
            log.info("User {} released lock on file {}", userId, fileId);
            return true;
        }
        return false;
    }

    public String getLockHolder(String fileId) {
        return fileLocks.get(fileId);
    }

    public boolean isLocked(String fileId) {
        return fileLocks.containsKey(fileId);
    }

    public void removeUserFromAllRooms(String userId) {
        // Remove from all project rooms
        rooms.forEach((projectId, users) -> users.remove(userId));
        rooms.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        // Remove from all file rooms and release locks
        fileRooms.forEach((fileId, users) -> {
            users.remove(userId);
            releaseLock(fileId, userId);
        });
        fileRooms.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        log.info("User {} removed from all rooms", userId);
    }
}

package by.nexus.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для управления комнатами WebSocket.
 * Использует Redis для хранения данных при масштабировании.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RedisTemplate<String, Object> redisTemplate;

    // In-memory fallback for local development (when Redis is not available)
    private final Map<String, Set<String>> localRooms = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> localFileRooms = new ConcurrentHashMap<>();
    private final Map<String, String> localFileLocks = new ConcurrentHashMap<>();

    private static final String ROOM_PREFIX = "nexus:room:";
    private static final String FILE_ROOM_PREFIX = "nexus:file:room:";
    private static final String FILE_LOCK_PREFIX = "nexus:file:lock:";
    private static final long LOCK_TTL_MINUTES = 30;

    /**
     * Добавляет пользователя в комнату проекта.
     */
    @SuppressWarnings("unchecked")
    public void joinRoom(String projectId, String userId) {
        try {
            String key = ROOM_PREFIX + projectId;
            Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
            if (users == null) {
                users = new HashSet<>();
            }
            users.add(userId);
            redisTemplate.opsForValue().set(key, users);
        } catch (Exception e) {
            // Fallback to local storage
            log.warn("Redis unavailable, using local storage");
            localRooms.computeIfAbsent(projectId, k -> new CopyOnWriteArraySet<>()).add(userId);
        }
        log.info("User {} joined room {}", userId, projectId);
    }

    /**
     * Удаляет пользователя из комнаты проекта.
     */
    @SuppressWarnings("unchecked")
    public void leaveRoom(String projectId, String userId) {
        try {
            String key = ROOM_PREFIX + projectId;
            Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    redisTemplate.delete(key);
                } else {
                    redisTemplate.opsForValue().set(key, users);
                }
            }
        } catch (Exception e) {
            // Fallback to local storage
            Set<String> room = localRooms.get(projectId);
            if (room != null) {
                room.remove(userId);
                if (room.isEmpty()) {
                    localRooms.remove(projectId);
                }
            }
        }
        log.info("User {} left room {}", userId, projectId);
    }

    /**
     * Возвращает список пользователей в комнате проекта.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRoomUsers(String projectId) {
        try {
            String key = ROOM_PREFIX + projectId;
            Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
            return users != null ? users : Set.of();
        } catch (Exception e) {
            return localRooms.getOrDefault(projectId, Set.of());
        }
    }

    /**
     * Добавляет пользователя в комнату файла.
     */
    @SuppressWarnings("unchecked")
    public void joinFileRoom(String fileId, String userId) {
        try {
            String key = FILE_ROOM_PREFIX + fileId;
            Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
            if (users == null) {
                users = new HashSet<>();
            }
            users.add(userId);
            redisTemplate.opsForValue().set(key, users);
        } catch (Exception e) {
            // Fallback to local storage
            localFileRooms.computeIfAbsent(fileId, k -> new CopyOnWriteArraySet<>()).add(userId);
        }
        log.info("User {} joined file room {}", userId, fileId);
    }

    /**
     * Удаляет пользователя из комнаты файла.
     */
    @SuppressWarnings("unchecked")
    public void leaveFileRoom(String fileId, String userId) {
        try {
            String key = FILE_ROOM_PREFIX + fileId;
            Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    redisTemplate.delete(key);
                } else {
                    redisTemplate.opsForValue().set(key, users);
                }
            }
        } catch (Exception e) {
            // Fallback to local storage
            Set<String> room = localFileRooms.get(fileId);
            if (room != null) {
                room.remove(userId);
                if (room.isEmpty()) {
                    localFileRooms.remove(fileId);
                }
            }
        }
        // Also release any lock held by this user
        releaseLock(fileId, userId);
        log.info("User {} left file room {}", userId, fileId);
    }

    /**
     * Возвращает список пользователей в комнате файла.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getFileRoomUsers(String fileId) {
        try {
            String key = FILE_ROOM_PREFIX + fileId;
            Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
            return users != null ? users : Set.of();
        } catch (Exception e) {
            return localFileRooms.getOrDefault(fileId, Set.of());
        }
    }

    /**
     * Пытается заблокировать файл для пользователя.
     *
     * @param fileId ID файла
     * @param userId ID пользователя
     * @return true если блокировка успешна
     */
    public boolean acquireLock(String fileId, String userId) {
        String key = FILE_LOCK_PREFIX + fileId;
        
        try {
            // Check if already locked by someone else
            String currentHolder = (String) redisTemplate.opsForValue().get(key);
            if (currentHolder != null && !currentHolder.equals(userId)) {
                log.warn("File {} is already locked by {}", fileId, currentHolder);
                return false;
            }
            
            // Set lock with TTL
            redisTemplate.opsForValue().set(key, userId, LOCK_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("User {} acquired lock on file {}", userId, fileId);
            return true;
        } catch (Exception e) {
            // Fallback to local storage
            String currentHolder = localFileLocks.get(fileId);
            if (currentHolder != null && !currentHolder.equals(userId)) {
                log.warn("File {} is already locked by {}", fileId, currentHolder);
                return false;
            }
            localFileLocks.put(fileId, userId);
            log.info("User {} acquired lock on file {} (local)", userId, fileId);
            return true;
        }
    }

    /**
     * Освобождает блокировку файла.
     *
     * @param fileId ID файла
     * @param userId ID пользователя
     * @return true если блокировка была освобождена
     */
    public boolean releaseLock(String fileId, String userId) {
        String key = FILE_LOCK_PREFIX + fileId;
        
        try {
            String currentHolder = (String) redisTemplate.opsForValue().get(key);
            if (currentHolder != null && currentHolder.equals(userId)) {
                redisTemplate.delete(key);
                log.info("User {} released lock on file {}", userId, fileId);
                return true;
            }
            return false;
        } catch (Exception e) {
            // Fallback to local storage
            String currentHolder = localFileLocks.get(fileId);
            if (currentHolder != null && currentHolder.equals(userId)) {
                localFileLocks.remove(fileId);
                log.info("User {} released lock on file {} (local)", userId, fileId);
                return true;
            }
            return false;
        }
    }

    /**
     * Возвращает ID пользователя, который заблокировал файл.
     */
    public String getLockHolder(String fileId) {
        String key = FILE_LOCK_PREFIX + fileId;
        
        try {
            return (String) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return localFileLocks.get(fileId);
        }
    }

    /**
     * Проверяет, заблокирован ли файл.
     */
    public boolean isLocked(String fileId) {
        return getLockHolder(fileId) != null;
    }

    /**
     * Удаляет пользователя из всех комнат.
     */
    public void removeUserFromAllRooms(String userId) {
        // Remove from all project rooms (Redis)
        try {
            Set<String> keys = redisTemplate.keys(ROOM_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    @SuppressWarnings("unchecked")
                    Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
                    if (users != null) {
                        users.remove(userId);
                        if (users.isEmpty()) {
                            redisTemplate.delete(key);
                        } else {
                            redisTemplate.opsForValue().set(key, users);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, using local storage for room cleanup");
        }
        
        // Remove from all file rooms (Redis)
        try {
            Set<String> keys = redisTemplate.keys(FILE_ROOM_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    @SuppressWarnings("unchecked")
                    Set<String> users = (Set<String>) redisTemplate.opsForValue().get(key);
                    if (users != null) {
                        users.remove(userId);
                        if (users.isEmpty()) {
                            redisTemplate.delete(key);
                        } else {
                            redisTemplate.opsForValue().set(key, users);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, using local storage for file room cleanup");
        }
        
        // Remove from local storage
        localRooms.forEach((projectId, users) -> users.remove(userId));
        localRooms.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        localFileRooms.forEach((fileId, users) -> {
            users.remove(userId);
            releaseLock(fileId, userId);
        });
        localFileRooms.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        log.info("User {} removed from all rooms", userId);
    }
}

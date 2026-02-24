package by.nexus.auth.service;

import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.UserRegistrationRequest;
import by.nexus.auth.model.entity.UserCredentials;

public interface UserCredentialsService {
    boolean existsByUsername(String username);
    UserCredentials getByUsername(String username);
    
    /**
     * Возвращает UserCredentials entity по username.
     *
     * @param username имя пользователя
     * @return UserCredentials entity
     */
    UserCredentials findByUsername(String username);
    
    /**
     * Возвращает AuthEntity DTO по username.
     *
     * @param username имя пользователя
     * @return AuthEntity DTO
     */
    AuthEntity findByUsernameAsDto(String username);
    
    AuthEntity save(AuthEntity authEntity);
    void deleteByUsername(String username);
    boolean register(UserRegistrationRequest request, String role);

    /**
     * @deprecated Используйте updateEncryptedMasterKey()
     */
    @Deprecated
    boolean setKey(String username, String key);
    
    /**
     * Обновляет зашифрованный master key и соль пользователя.
     *
     * @param username имя пользователя
     * @param encryptedMasterKey зашифрованный master key
     * @param keySalt соль для PBKDF2
     * @return true если обновление успешно
     */
    boolean updateEncryptedMasterKey(String username, String encryptedMasterKey, String keySalt);
}

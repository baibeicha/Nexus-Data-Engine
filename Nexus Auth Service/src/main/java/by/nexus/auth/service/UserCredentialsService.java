package by.nexus.auth.service;

import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.UserRegistrationRequest;
import by.nexus.auth.model.entity.UserCredentials;

public interface UserCredentialsService {
    boolean existsByUsername(String username);
    UserCredentials getByUsername(String username);
    AuthEntity findByUsername(String username);
    AuthEntity save(AuthEntity authEntity);
    void deleteByUsername(String username);
    boolean register(UserRegistrationRequest request, String role);

    boolean setKey(String username, String key);
}

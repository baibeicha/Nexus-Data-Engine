package by.nexus.auth.service.impl;

import by.nexus.auth.exception.UserCredentialsNotFoundException;
import by.nexus.auth.mapper.UserCredentialsMapper;
import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.UserRegistrationRequest;
import by.nexus.auth.model.entity.UserCredentials;
import by.nexus.auth.repository.UserCredentialsRepository;
import by.nexus.auth.service.CryptoService;
import by.nexus.auth.service.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialsMapper userCredentialsMapper;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    @Override
    public boolean existsByUsername(String username) {
        return userCredentialsRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public UserCredentials getByUsername(String username) {
        return userCredentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserCredentialsNotFoundException(username));
    }

    @Transactional(readOnly = true)
    @Override
    public UserCredentials findByUsername(String username) {
        return getByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public AuthEntity findByUsernameAsDto(String username) {
        return userCredentialsMapper.toDto(getByUsername(username));
    }

    @Transactional
    @Override
    public AuthEntity save(AuthEntity authEntity) {
        UserCredentials userCredentials =
                userCredentialsRepository.findByUsername(authEntity.username()).orElse(null);
        if (userCredentials == null) {
            userCredentials = userCredentialsMapper.toEntity(authEntity);
        } else {
            userCredentials = userCredentialsMapper.merge(userCredentials, authEntity);
        }

        userCredentials = userCredentialsRepository.save(userCredentials);
        return userCredentialsMapper.toDto(userCredentials);
    }

    @Transactional
    @Override
    public void deleteByUsername(String username) {
        userCredentialsRepository.deleteByUsername(username);
    }

    @Transactional
    @Override
    public boolean register(UserRegistrationRequest request, String role) {
        try {
            if (existsByUsername(request.getUsername())) {
                log.warn("User already exists: {}", request.getUsername());
                return false;
            }

            // Генерируем соль для PBKDF2
            String keySalt = cryptoService.generateSalt();
            
            // Генерируем случайный master key
            String masterKey = cryptoService.generateMasterKey();
            
            // Шифруем master key с использованием derived key из пароля
            String encryptedMasterKey = cryptoService.encryptMasterKey(masterKey, request.getPassword(), keySalt);

            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setUsername(request.getUsername());
            userCredentials.setPassword(passwordEncoder.encode(request.getPassword()));
            userCredentials.setRole(role);
            userCredentials.setEnabled(true);
            userCredentials.setEncryptedMasterKey(encryptedMasterKey);
            userCredentials.setKeySalt(keySalt);

            userCredentialsRepository.save(userCredentials);
            
            log.info("User registered successfully: {}", request.getUsername());
            return true;
        } catch (Exception e) {
            log.error("Failed to register user: {}", request.getUsername(), e);
            return false;
        }
    }

    @Transactional
    @Override
    public boolean setKey(String username, String key) {
        try {
            if (!existsByUsername(username)) {
                log.warn("User not found: {}", username);
                return false;
            }

            UserCredentials userCredentials = getByUsername(username);
            userCredentials.setEncryptedMasterKey(key);
            userCredentialsRepository.save(userCredentials);

            log.info("Master key updated for user: {}", username);
            return true;
        } catch (Exception e) {
            log.error("Failed to set key for user: {}", username, e);
            return false;
        }
    }

    @Transactional
    @Override
    public boolean updateEncryptedMasterKey(String username, String encryptedMasterKey, String keySalt) {
        try {
            if (!existsByUsername(username)) {
                log.warn("User not found: {}", username);
                return false;
            }

            UserCredentials userCredentials = getByUsername(username);
            userCredentials.setEncryptedMasterKey(encryptedMasterKey);
            userCredentials.setKeySalt(keySalt);
            userCredentialsRepository.save(userCredentials);

            log.info("Encrypted master key updated for user: {}", username);
            return true;
        } catch (Exception e) {
            log.error("Failed to update encrypted master key for user: {}", username, e);
            return false;
        }
    }
}

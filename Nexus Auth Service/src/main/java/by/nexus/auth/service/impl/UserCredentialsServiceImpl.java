package by.nexus.auth.service.impl;

import by.nexus.auth.exception.UserCredentialsNotFoundException;
import by.nexus.auth.mapper.UserCredentialsMapper;
import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.UserRegistrationRequest;
import by.nexus.auth.model.entity.UserCredentials;
import by.nexus.auth.repository.UserCredentialsRepository;
import by.nexus.auth.service.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialsMapper userCredentialsMapper;

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
    public AuthEntity findByUsername(String username) {
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
                return false;
            }

            AuthEntity authEntity = save(new AuthEntity(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    role
            ));

            return authEntity != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    @Override
    public boolean setKey(String username, String key) {
        try {
            if (existsByUsername(username)) {
                return false;
            }

            UserCredentials userCredentials = getByUsername(username);
            userCredentials.setKey(key);
            userCredentialsRepository.save(userCredentials);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

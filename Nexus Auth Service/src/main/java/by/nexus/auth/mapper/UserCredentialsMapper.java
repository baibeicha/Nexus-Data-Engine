package by.nexus.auth.mapper;

import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.entity.UserCredentials;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialsMapper extends BaseMapper<UserCredentials, AuthEntity> {

    @Override
    public AuthEntity toDto(UserCredentials userCredentials) {
        return new AuthEntity(
                userCredentials.getUsername(),
                userCredentials.getPassword(),
                userCredentials.getRole()
        );
    }

    @Override
    public UserCredentials toEntity(AuthEntity authEntity) {
        UserCredentials uc = new UserCredentials();
        uc.setUsername(authEntity.username());
        uc.setPassword(authEntity.password());
        uc.setRole(authEntity.role());
        return uc;
    }

    @Override
    public UserCredentials merge(UserCredentials userCredentials, AuthEntity authEntity) {
        if (authEntity == null) {
            return userCredentials;
        } else if (authEntity.username() != null && authEntity.username().isEmpty()) {
            userCredentials.setUsername(userCredentials.getUsername());
        } else if (authEntity.password() != null && authEntity.password().isEmpty()) {
            userCredentials.setPassword(userCredentials.getPassword());
        } else if (authEntity.role() != null && authEntity.role().isEmpty()) {
            userCredentials.setRole(userCredentials.getRole());
        }
        return userCredentials;
    }
}

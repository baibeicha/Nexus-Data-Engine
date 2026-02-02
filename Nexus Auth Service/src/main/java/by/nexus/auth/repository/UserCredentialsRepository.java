package by.nexus.auth.repository;

import by.nexus.auth.model.entity.UserCredentials;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserCredentialsRepository extends CrudRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUsername(String username);
    void deleteByUsername(String username);
    boolean existsByUsername(String username);
}

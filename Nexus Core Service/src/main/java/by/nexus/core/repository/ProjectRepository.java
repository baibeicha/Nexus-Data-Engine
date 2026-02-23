package by.nexus.core.repository;

import by.nexus.core.model.entity.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends CrudRepository<Project, UUID> {
    boolean existsByNameAndOwnerId(String name, String ownerId);
    Optional<Project> findByNameAndOwnerId(String name, String ownerId);
    List<Project> findAllByOwnerId(String userId);
}

package by.nexus.core.repository;

import by.nexus.core.model.entity.ProjectPermission;
import org.springframework.data.repository.CrudRepository;

public interface ProjectPermissionRepository
        extends CrudRepository<ProjectPermission, ProjectPermission.ProjectPermissionId> {
}

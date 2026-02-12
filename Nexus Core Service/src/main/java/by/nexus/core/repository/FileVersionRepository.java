package by.nexus.core.repository;

import by.nexus.core.model.entity.FileVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FileVersionRepository extends CrudRepository<FileVersion, UUID> {
    List<FileVersion> findAllByFileNodeId(UUID id);
}

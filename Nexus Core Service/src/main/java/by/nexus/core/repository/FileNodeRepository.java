package by.nexus.core.repository;

import by.nexus.core.model.entity.FileNode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FileNodeRepository extends CrudRepository<FileNode, UUID> {

    @Query("SELECT f FROM FileNode f WHERE f.project.id = :projectId ORDER BY f.type ASC, f.name ASC")
    List<FileNode> findAllByProjectSorted(@Param("projectId") UUID projectId);

    boolean existsByParentIdAndName(UUID parentId, String name);
}

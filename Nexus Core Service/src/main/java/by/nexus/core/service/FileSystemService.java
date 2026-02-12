package by.nexus.core.service;

import by.nexus.core.model.entity.FileNode;
import by.nexus.core.model.entity.FileVersion;
import by.nexus.core.model.entity.Project;
import by.nexus.core.model.entity.ProjectPermission;

import java.util.List;
import java.util.UUID;

public interface FileSystemService {
    Project createProject(String userId, String projectName, String description);
    void deleteProject(String userId, String projectName);
    FileNode createNode(UUID projectId, UUID parentId, String name, FileNode.FileType type, String userId);
    void deleteNode(UUID nodeId, String userId);
    void moveNode(UUID nodeId, UUID targetParentId, String userId);
    void renameNode(UUID nodeId, String newName, String userId);
    List<FileNode> listFiles(UUID projectId);
    void commitVersion(UUID nodeId);
    List<FileVersion> getHistory(UUID nodeId);
    void rollbackToVersion(UUID nodeId, Integer version);
    void rollbackToVersion(UUID nodeId, UUID versionId);
    void checkPermission(String userId, UUID projectId, ProjectPermission.AccessLevel level);
    void shareProject(UUID projectId, String userId, ProjectPermission.AccessLevel level);
    void updateVersionSize(UUID nodeId, long size, String userId);
}

package by.nexus.core.service;

import by.nexus.core.model.entity.FileNode;
import by.nexus.core.model.entity.FileVersion;
import by.nexus.core.model.entity.Project;
import by.nexus.core.model.entity.ProjectPermission;

import java.util.List;
import java.util.TreeMap;

public interface FileSystemService {
    Project createProject(String userId, String projectName, String description);
    void deleteProject(String userId, String projectName);
    FileNode createNode(Long projectId, Long parentId, String name, FileNode.FileType type);
    void deleteNode(Long nodeId);
    void moveNode(Long nodeId, Long targetParentId);
    void renameNode(Long nodeId, String newName);
    TreeMap<Long, FileNode> listFiles(Long projectId);
    TreeMap<Long, FileNode> listFiles(Long projectId, Long parentId);
    void commitVersion(Long projectId, Long nodeId);
    List<FileVersion> getHistory(Long projectId, Long nodeId);
    void rollbackToVersion(Long projectId, Long nodeId, Integer version);
    void rollbackToVersion(Long projectId, Long nodeId, Long versionId);
    boolean hasAccess(String userId, Long projectId, ProjectPermission.AccessLevel level);
    boolean checkPermission(String userId, Long projectId, ProjectPermission.AccessLevel level);
    void shareProject(Long projectId, String userId, ProjectPermission.AccessLevel level);
}

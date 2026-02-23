package by.nexus.core.model.dto;

public record DatabaseConnectionDetails(
        String url,
        String user,
        String password
) {
}

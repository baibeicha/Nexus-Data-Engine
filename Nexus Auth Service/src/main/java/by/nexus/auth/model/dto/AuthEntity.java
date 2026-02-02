package by.nexus.auth.model.dto;

public record AuthEntity(
        String username,
        String password,
        String role
) {
}

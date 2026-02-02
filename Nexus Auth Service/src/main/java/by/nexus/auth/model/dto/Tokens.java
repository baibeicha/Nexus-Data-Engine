package by.nexus.auth.model.dto;

public record Tokens (
        String accessToken,
        String refreshToken
) {
}

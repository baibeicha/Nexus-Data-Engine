package by.nexus.auth.service;

import by.nexus.auth.model.dto.AuthEntity;

public interface JwtService {
    String extractUsername(String token);

    String extractRole(String token);

    String generateAccessToken(AuthEntity auth);
    String generateRefreshToken(AuthEntity auth);
    boolean isTokenValid(String token);
    String refreshAccessToken(String refreshToken, AuthEntity auth);
}

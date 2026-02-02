package by.nexus.auth.service.impl;

import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.entity.UserCredentials;
import by.nexus.auth.service.JwtService;
import by.nexus.auth.service.UserCredentialsService;
import by.nexus.auth.util.TimeParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JjwtService implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    private final UserCredentialsService userCredentialsService;

    public JjwtService(UserCredentialsService userCredentialsService, TimeParser timeParser,
                       @Value("${jwt.accessToken.expiration}") String accessTokenExpiration,
                       @Value("${jwt.refreshToken.expiration}") String refreshTokenExpiration) {
        this.userCredentialsService = userCredentialsService;

        this.accessTokenExpiration = timeParser.parseTime(accessTokenExpiration);
        this.refreshTokenExpiration = timeParser.parseTime(refreshTokenExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String generateAccessToken(AuthEntity auth) {
        return buildToken(auth.username(), accessTokenExpiration, auth.role());
    }

    @Override
    public String generateRefreshToken(AuthEntity auth) {
        return buildToken(auth.username(), refreshTokenExpiration, auth.role());
    }

    private String buildToken(String subject, long expiration, String role) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("role", role)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            final String username = extractUsername(token);
            final UserCredentials userCredentials = userCredentialsService.getByUsername(username);
            return username != null && !username.isEmpty() && !isTokenExpired(token)
                    && userCredentials.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public String refreshAccessToken(String refreshToken, AuthEntity auth) {
        if (isTokenValid(refreshToken)) {
            return generateAccessToken(auth);
        } else {
            throw new SecurityException("Invalid refresh token");
        }
    }
}

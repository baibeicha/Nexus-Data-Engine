package by.nexus.auth.service.impl;

import by.nexus.auth.exception.UserCredentialsNotFoundException;
import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.Tokens;
import by.nexus.auth.service.JwtService;
import by.nexus.auth.service.TokensService;
import by.nexus.auth.service.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokensServiceImpl implements TokensService {

    private UserCredentialsService userCredentialsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public void setUserCredentialsService(@Lazy UserCredentialsService userCredentialsService) {
        this.userCredentialsService = userCredentialsService;
    }

    @Override
    public Tokens generateTokens(AuthEntity auth) {
        if (!userCredentialsService.existsByUsername(auth.username())) {
            throw new UserCredentialsNotFoundException(auth.username());
        }

        AuthEntity exists = userCredentialsService.findByUsername(auth.username());
        if (!passwordEncoder.matches(auth.password(), exists.password())) {
            throw new SecurityException("Wrong password");
        }

        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);
        return new Tokens(accessToken, refreshToken);
    }

    @Override
    public Tokens refreshTokens(Tokens tokens) {
        AuthEntity auth = userCredentialsService.findByUsername(tokens.accessToken());
        String newAccessToken = jwtService.refreshAccessToken(tokens.accessToken(), auth);
        return new Tokens(newAccessToken, tokens.refreshToken());
    }
}

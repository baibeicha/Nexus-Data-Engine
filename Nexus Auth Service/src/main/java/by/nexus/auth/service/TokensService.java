package by.nexus.auth.service;

import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.Tokens;

public interface TokensService {
    Tokens generateTokens(AuthEntity auth);
    Tokens refreshTokens(Tokens tokens);
}

package by.nexus.auth.controller.api.v1;

import by.nexus.auth.model.dto.AuthEntity;
import by.nexus.auth.model.dto.MasterKeyResponse;
import by.nexus.auth.model.dto.Tokens;
import by.nexus.auth.model.dto.UserRegistrationRequest;
import by.nexus.auth.model.entity.UserCredentials;
import by.nexus.auth.service.JwtService;
import by.nexus.auth.service.TokensService;
import by.nexus.auth.service.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserCredentialsService userCredentialsService;
    private final TokensService tokensService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userCredentialsService.register(request, "USER"));
    }

    @PostMapping("/tokens")
    public ResponseEntity<Tokens> tokens(@RequestBody AuthEntity authEntity) {
        return ResponseEntity.ok(tokensService.generateTokens(authEntity));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestBody String token) {
        return ResponseEntity.ok(jwtService.isTokenValid(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Tokens> refresh(@RequestBody Tokens tokens) {
        return ResponseEntity.ok(tokensService.refreshTokens(tokens));
    }

    /**
     * Возвращает зашифрованный master key пользователя и соль.
     * Клиент должен использовать пароль пользователя для расшифровки.
     *
     * @param authentication текущий аутентифицированный пользователь
     * @return зашифрованный master key и соль
     */
    @GetMapping("/master-key")
    public ResponseEntity<MasterKeyResponse> getMasterKey(Authentication authentication) {
        String username = authentication.getName();
        UserCredentials user = userCredentialsService.findByUsername(username);
        
        MasterKeyResponse response = new MasterKeyResponse(
                user.getEncryptedMasterKey(),
                user.getKeySalt()
        );
        
        return ResponseEntity.ok(response);
    }
}

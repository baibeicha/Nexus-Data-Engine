package by.nexus.auth.controller.api.v1;

import by.nexus.auth.service.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/key")
@RequiredArgsConstructor
public class UserKeyController {

    private final UserCredentialsService userCredentialsService;

    @GetMapping
    public String getKey(Authentication authentication) {
        return userCredentialsService.getByUsername(authentication.getName()).getKey();
    }

    @PostMapping
    public Boolean setKey(Authentication authentication, @RequestBody String key) {
        return userCredentialsService.setKey(authentication.getName(), key);
    }
}

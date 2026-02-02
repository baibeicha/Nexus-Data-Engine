package by.nexus.auth.controller.api.v1;

import by.nexus.auth.model.dto.UserRegistrationRequest;
import by.nexus.auth.service.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserCredentialsService userCredentialsService;

    @PostMapping("/register/{role}")
    public boolean register(@PathVariable String role, @RequestBody UserRegistrationRequest request) {
        return userCredentialsService.register(request, role);
    }

    @PostMapping("/delete/{username}")
    public ResponseEntity<?> delete(@PathVariable String username) {
        userCredentialsService.deleteByUsername(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

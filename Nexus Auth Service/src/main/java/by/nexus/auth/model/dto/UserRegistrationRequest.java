package by.nexus.auth.model.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
}

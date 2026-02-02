package by.nexus.auth.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}

package org.example.dto.response;

import lombok.Data;
import org.example.enums.Role; // Rol enum'ınızın paketi

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String specialty; // Doktorlar için
}
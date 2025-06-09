package org.example.dto.request;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
public class UserRegistrationRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6, max = 100)
    private String password;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String specialty; // Doktorlar için, hastalar için boş/null bırakılacak
}



package org.example.dto.response;

// response/AuthResponse.java
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

        public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer"; // Varsayılan değeri burada set ediyoruz
    }
}


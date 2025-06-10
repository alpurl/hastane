package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Lombok kullanıyorsanız NoArgsConstructor da eklemek iyi olur

@Data
@AllArgsConstructor // Tüm argümanları alan constructor
@NoArgsConstructor  // Argümansız constructor (JSON deserializasyonu için)
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId; // Kullanıcı ID'si
    private String username; // Kullanıcı Adı
    private String role; // Kullanıcının ana rolü (örn: "HASTA", "DOKTOR")
    // private List<String> roles; // Eğer birden fazla rolü gönderecekseniz

        // <-- Buradaki custom constructor'ı mı kopyaladınız?
    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer"; // Varsayılan değeri burada set ediyoruz
    }
}
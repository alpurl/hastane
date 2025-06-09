package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * İstemcinin hatalı bir istek gönderdiğinde (örn: geçersiz veri, iş kuralı ihlali)
 * fırlatılan özel istisna sınıfı.
 * Spring'in @ResponseStatus anotasyonu ile HTTP 400 BAD REQUEST durum kodu ile eşleştirilmiştir.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Bu anotasyon, Controller'da yakalanmadan da doğrudan 400 döndürmesini sağlar.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

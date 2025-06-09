package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Belirtilen bir kaynak bulunamadığında fırlatılan özel istisna sınıfı.
 * Spring'in @ResponseStatus anotasyonu ile HTTP 404 NOT FOUND durum kodu ile eşleştirilmiştir.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Bu anotasyon, Controller'da yakalanmadan da doğrudan 404 döndürmesini sağlar.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
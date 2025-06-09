package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // @Valid ile ilgili hatalar için
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * Uygulama genelindeki istisnaları merkezi olarak yöneten bir sınıf.
 * Bu sınıf, fırlatılan istisnaları yakalar ve uygun HTTP yanıtlarını döndürür.
 */
@ControllerAdvice // Bu sınıfın tüm Controller'lara "advice" (tavsiye) sağlayacağını belirtir
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Özel Exception'lar için Handler'lar

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Yaygın Spring ve diğer genel Exception'lar için Handler'lar

    @ExceptionHandler(Exception.class) // Tüm diğer beklenmedik istisnalar için genel yakalayıcı
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Beklenmedik bir hata oluştu: " + ex.getMessage(), // Daha genel bir mesaj verebiliriz
                request.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        // Log the exception for debugging purposes (e.g., using SLF4J and Logback)
        // logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // @Valid ile gelen doğrulama hataları için özelleştirilmiş handler
    // ResponseEntityExceptionHandler sınıfının handleMethodArgumentNotValid metodunu override ediyoruz
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  org.springframework.http.HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Giriş verisi doğrulama hatası",
                errors.toString(), // Hataları JSON olarak döndürmek için daha iyi formatlayabiliriz
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    // Yardımcı sınıf: Hata detaylarını içeren yanıt objesi
    public static class ErrorDetails {
        private LocalDateTime timestamp;
        private String message;
        private String details;
        private int status;

        public ErrorDetails(LocalDateTime timestamp, String message, String details, int status) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
            this.status = status;
        }

        // Getter metotları (Lombok @Data kullanıyorsanız otomatik oluşur)
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public int getStatus() { return status; }
    }
}

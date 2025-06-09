package org.example.controller;

import org.example.model.User;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.exception.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.security.UserPrincipal;




@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection ile bağımlılıkları enjekte ediyoruz
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Not: Kayıt (/register) ve Giriş (/login) endpoint'leri AuthController'a taşınmıştır.
    // Bu UserController genellikle kullanıcı profil işlemleri veya admin tarafındaki kullanıcı yönetimi için kullanılır.

    /**
     * Kimliği doğrulanmış kullanıcının kendi profil bilgilerini görmesini sağlar.
     * Hem hasta hem de doktor kendi profilini görebilir.
     *
     * @param currentUser Giriş yapmış kullanıcının bilgileri
     * @return Kullanıcının User nesnesiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')") // Hem hasta hem doktor kendi profilini görebilir
    @GetMapping("/me") // /api/users/me
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        // currentUser.getId() metodu, UserPrincipal implementasyonunuza göre kullanıcının ID'sini döndürmelidir.
        // Bu ID ile kullanıcıyı veritabanından tekrar çekmek en güvenli yöntemdir.
        User user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Belirli bir kullanıcının (genellikle kendi profilini) ID ile görüntülenmesini sağlar.
     * Bu endpoint'e doğrudan erişimi kısıtlamak gerekebilir (örn: sadece admin veya ilgili kullanıcı).
     * Yukarıdaki '/me' endpoint'i daha güvenli bir alternatiftir.
     *
     * @param userId Görüntülenecek kullanıcının ID'si
     * @param currentUser Giriş yapan kullanıcının bilgileri (yetkilendirme için)
     * @return Kullanıcının User nesnesiyle HTTP 200 OK yanıtı
     */
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')") // Sadece yetkili kullanıcılar erişebilir
    @GetMapping("/{userId}") // /api/users/{userId}
    public ResponseEntity<User> getUserById(@PathVariable Long userId, @AuthenticationPrincipal UserPrincipal currentUser) {
        // Güvenlik kontrolü: Kullanıcı sadece kendi profilini veya admin diğer kullanıcıların profilini görebilir.
        // Bu projede admin rolü yok, bu yüzden sadece kendi profilini görebilir.
        if (!userId.equals(currentUser.getId())) {
            throw new BadRequestException("Başka bir kullanıcının profiline erişim yetkiniz yok.");
        }
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Sistemdeki tüm doktorların listesini döndürür.
     * Hastalar randevu alırken bu listeyi görebilir.
     *
     * @return Doktor rolündeki kullanıcıların listesi
     */
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')") // Hastalar randevu almak için, doktorlar bilgi için görebilir
    @GetMapping("/doctors") // /api/users/doctors
    public ResponseEntity<List<User>> getAllDoctors() {
        List<User> doctors = userService.getAllDoctors();
        // Şifre gibi hassas bilgileri DTO kullanarak dışarıya vermemeye dikkat edin!
        // Burada User objesi döndürülüyor, ancak gerçek uygulamada UserResponseDTO kullanılmalı.
        return ResponseEntity.ok(doctors);
    }

    // Not: Kullanıcı profili güncelleme veya silme gibi işlemler bu controller'a eklenebilir.
}

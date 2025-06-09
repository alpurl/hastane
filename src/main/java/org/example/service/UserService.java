package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import org.example.dto.request.UserRegistrationRequest;
import org.example.enums.Role;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Şifreleme için bağımlılık

    // @Autowired yerine Constructor Injection tercih edilir.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Yeni bir kullanıcı (Hasta veya Doktor) kaydeder.
     * Şifreyi hash'ler ve kullanıcı adı/e-posta benzersizliğini kontrol eder.
     *
     * @param request Kayıt isteği DTO'su
     * @param role    Kullanıcının rolü (HASTA veya DOKTOR)
     * @return Kaydedilen User nesnesi
     * @throws BadRequestException Kullanıcı adı veya e-posta zaten kullanımda ise
     */
    @Transactional // Veritabanı yazma işlemi olduğu için @Transactional ekliyoruz
    public User registerUser(UserRegistrationRequest request, Role role) {
        // Kullanıcı adı ve e-posta benzersizlik kontrolü
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Kullanıcı adı '" + request.getUsername() + "' zaten kullanımda!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("E-posta '" + request.getEmail() + "' zaten kullanımda!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Şifreyi hash'leyerek kaydet
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setRole(role);

        // Eğer rol doktor ise uzmanlık alanını set et
        if (role == Role.DOKTOR) {
            // Doktor kaydında uzmanlık alanı boş olmamalı kontrolü
            if (request.getSpecialty() == null || request.getSpecialty().trim().isEmpty()) {
                throw new BadRequestException("Doktor kaydı için uzmanlık alanı boş bırakılamaz.");
            }
            newUser.setSpecialty(request.getSpecialty());
        } else { // Hasta ise uzmanlık alanı null olmalı
            newUser.setSpecialty(null); // Hastalar için uzmanlık alanı set edilmemeli
        }

        return userRepository.save(newUser);
    }

    /**
     * Belirtilen kullanıcı adına sahip kullanıcıyı bulur.
     * Bu metot Spring Security'nin UserDetailsService'i tarafından kullanılabilir.
     *
     * @param username Kullanıcı adı
     * @return User nesnesi
     * @throws ResourceNotFoundException Kullanıcı bulunamazsa
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
    }

    /**
     * Belirtilen ID'ye sahip kullanıcıyı bulur.
     *
     * @param id Kullanıcı ID'si
     * @return User nesnesi
     * @throws ResourceNotFoundException Kullanıcı bulunamazsa
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id));
    }

    /**
     * Sistemdeki tüm doktor kullanıcılarını listeler.
     *
     * @return Doktor rolündeki kullanıcıların listesi
     */
    public List<User> getAllDoctors() {
        return userRepository.findByRole(Role.DOKTOR);
    }
}

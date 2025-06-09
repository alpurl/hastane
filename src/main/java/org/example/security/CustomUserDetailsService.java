package org.example.security;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Veritabanı işlemi için

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // Lazy loading'in çalışabilmesi için
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Login sırasında kullanıcı adı veya e-posta ile kullanıcıyı bul
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("Kullanıcı bulunamadı: " + usernameOrEmail)
                        ));

        return UserPrincipal.create(user);
    }

    // JWT token'dan gelen userId ile kullanıcıyı yüklemek için (isteğe bağlı ama yaygın)
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + id)
        );
        return UserPrincipal.create(user);
    }
}
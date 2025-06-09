package org.example.security;

import org.example.model.User;
import org.example.enums.Role; // Role enum'ı
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Role role; // Kullanıcının rolü

    // Roller, Spring Security'nin yetkilendirme sistemi için kullanılır
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password, Role role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
    }

    // User modelinden UserPrincipal oluşturmak için statik bir metot
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        // Role enum'ınızın ismini büyük harfe dönüştürerek "ROLE_" prefix'i eklemek Spring Security'nin varsayılan davranışına uyar.
        // Örneğin, Role.PATIENT -> "ROLE_PATIENT"

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Uygulamanızda hesap süresinin dolup dolmadığı kontrolü yoksa true döndürün
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Uygulamanızda hesap kilitleme özelliği yoksa true döndürün
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Uygulamanızda parola süresinin dolup dolmadığı kontrolü yoksa true döndürün
    }

    @Override
    public boolean isEnabled() {
        return true; // Uygulamanızda hesap etkinleştirme/devre dışı bırakma yoksa true döndürün
    }

    // equals ve hashCode metotları, Set veya Map gibi koleksiyonlarda UserPrincipal'ı düzgün kullanmak için önemlidir.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id); // ID'ye göre eşitliği kontrol eder
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
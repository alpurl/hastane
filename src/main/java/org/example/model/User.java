package org.example.model;

import javax.persistence.Entity;

import org.example.enums.Role;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data // Lombok: Getters, Setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password; // Hashlenmiş şifre
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING) // Enum'ı string olarak kaydet
    @Column(nullable = false)
    private Role role; // PATIENT veya DOCTOR

    // Doktorlar için özel alan (Hastalar için NULL olacak)
    private String specialty; // Uzmanlık alanı (örn: "Kardiyoloji")

  
}

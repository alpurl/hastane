package org.example.model;

import org.example.enums.Role;

import javax.persistence.*;

@Entity
public class User {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    // getter/setter
}

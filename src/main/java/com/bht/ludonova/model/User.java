package com.bht.ludonova.model;

import com.bht.ludonova.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "steam_id")
    @JsonManagedReference
    private SteamUser steamUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;  // Default to USER role
}
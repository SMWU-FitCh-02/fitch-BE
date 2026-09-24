package com.vocal.app.user.entity;

import com.vocal.app.global.enums.Gender;
import com.vocal.app.global.enums.Role;
import com.vocal.app.global.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    private LocalDate birthDate;

    private String phoneNumber;

    // 클릭한 순서를 유지한 콤마 구분 문자열 (예: "발라드,댄스,POP")
    @Column(length = 100)
    private String preferredGenres;

    //역할 (관리자, 사용자)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SocialType socialType = SocialType.LOCAL;

    @Column(unique = true)
    private String socialUid;

    // base64 data URL (e.g. "data:image/jpeg;base64,...") — @Lob so it maps to a
    // LONGTEXT column instead of the default varchar(255)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
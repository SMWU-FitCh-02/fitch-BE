package com.vocal.app.user.dto;

import com.vocal.app.global.enums.Gender;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String phoneNumber;
    private Gender gender;
    private String profileImage;
    private LocalDateTime createdAt;
}
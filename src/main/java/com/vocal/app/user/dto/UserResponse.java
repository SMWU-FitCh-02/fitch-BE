package com.vocal.app.user.dto;

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
    private LocalDateTime createdAt;
}
package com.vocal.app.user.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
public class UpdateUserRequest {
    private String name;
    private String nickname;
    private String email;
    private LocalDate birthDate;
    private String phoneNumber;
}
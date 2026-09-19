package com.vocal.app.user.dto;

import com.vocal.app.global.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

    @Email
    @NotBlank
    private String email;

    private LocalDate birthDate;

    private String phoneNumber;

    private Gender gender;
}
package com.vocal.app.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class RegisterRequest {
    @Email
    @NotBlank
    private String email;


    @NotBlank
    @Size(min = 8)
    private String password;


    @NotBlank
    private String nickname;
}

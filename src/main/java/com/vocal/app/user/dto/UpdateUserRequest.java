package com.vocal.app.user.dto;

import com.vocal.app.global.enums.Gender;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
public class UpdateUserRequest {
    private String name;
    private String nickname;
    private String email;
    private LocalDate birthDate;
    private String phoneNumber;
    private Gender gender;
    private String profileImage;
    private String preferredGenres;
}
package com.vocal.app.user.dto;

import lombok.*;

@Getter @Builder @AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}

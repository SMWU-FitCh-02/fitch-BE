package com.vocal.app.user.service;

import com.vocal.app.user.dto.*;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import com.vocal.app.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        return userRepository.save(User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build()).getUserId();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        return buildToken(user.getEmail());
    }

    public TokenResponse refresh(RefreshRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken()))
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        return buildToken(tokenProvider.getEmail(request.getRefreshToken()));
    }

    private TokenResponse buildToken(String email) {
        return TokenResponse.builder()
                .accessToken(tokenProvider.createAccessToken(email))
                .refreshToken(tokenProvider.createRefreshToken(email))
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .build();
    }
}

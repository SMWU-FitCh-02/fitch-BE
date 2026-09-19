package com.vocal.app.user.service;

import com.vocal.app.user.dto.*;
import com.vocal.app.user.entity.User;
import com.vocal.app.global.enums.Role;
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
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + request.getUsername());

        return userRepository.save(User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .role(Role.USER)
                .build()).getUserId();
    }

    public boolean checkUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        return buildToken(user.getUsername());
    }

    public TokenResponse refresh(RefreshRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken()))
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        return buildToken(tokenProvider.getEmail(request.getRefreshToken()));
    }

    private TokenResponse buildToken(String subject) {
        return TokenResponse.builder()
                .accessToken(tokenProvider.createAccessToken(subject))
                .refreshToken(tokenProvider.createRefreshToken(subject))
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .build();
    }
}
package com.vocal.app.user.controller;

import com.vocal.app.user.dto.UserResponse;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import com.vocal.app.user.service.VocalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final VocalHistoryService vocalHistoryService;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원: " + userId));
        return ResponseEntity.ok(UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @GetMapping("/{id}/vocal-history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(vocalHistoryService.getHistory(userId));
    }
}
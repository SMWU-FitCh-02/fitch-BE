package com.vocal.app.user.controller;

import com.vocal.app.bookmark.repository.ChartLikeRepository;
import com.vocal.app.bookmark.repository.SongBookmarkRepository;
import com.vocal.app.user.dto.UserResponse;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import com.vocal.app.user.repository.VocalHistoryRepository;
import com.vocal.app.user.service.VocalHistoryService;
import com.vocal.app.voice.repository.VocalRangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vocal.app.user.dto.UpdateUserRequest;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final VocalHistoryService vocalHistoryService;
    private final VocalHistoryRepository vocalHistoryRepository;
    private final VocalRangeRepository vocalRangeRepository;
    private final SongBookmarkRepository songBookmarkRepository;
    private final ChartLikeRepository chartLikeRepository;
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
                .gender(user.getGender())
                .preferredGenres(user.getPreferredGenres())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @GetMapping("/{id}/vocal-history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(vocalHistoryService.getHistory(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long userId,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원: " + userId));

        if (!user.getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 정보만 수정할 수 있습니다.");
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPreferredGenres() != null) user.setPreferredGenres(request.getPreferredGenres());
        user.setProfileImage(request.getProfileImage());
        userRepository.save(user);

        return ResponseEntity.ok(UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .preferredGenres(user.getPreferredGenres())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable("id") Long userId,
            Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원: " + userId));

        if (!user.getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정만 탈퇴할 수 있습니다.");
        }

        vocalHistoryRepository.deleteByUserUserId(userId);
        vocalRangeRepository.deleteByUserUserId(userId);
        songBookmarkRepository.deleteByUserUserId(userId);
        chartLikeRepository.deleteByUserUserId(userId);
        userRepository.deleteById(userId);
        return ResponseEntity.ok(Map.of("message", "탈퇴 처리되었습니다."));
    }
}
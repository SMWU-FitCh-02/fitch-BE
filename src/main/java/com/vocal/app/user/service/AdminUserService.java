package com.vocal.app.user.service;

import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import com.vocal.app.user.repository.VocalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final VocalHistoryRepository vocalHistoryRepository;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new IllegalArgumentException("존재하지 않는 회원: " + userId);
        vocalHistoryRepository.deleteByUserUserId(userId);
        userRepository.deleteById(userId);
    }
}
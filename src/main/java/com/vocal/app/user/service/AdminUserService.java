package com.vocal.app.user.service;

import com.vocal.app.bookmark.repository.ChartLikeRepository;
import com.vocal.app.bookmark.repository.SongBookmarkRepository;
import com.vocal.app.user.entity.User;
import com.vocal.app.user.repository.UserRepository;
import com.vocal.app.user.repository.VocalHistoryRepository;
import com.vocal.app.voice.repository.VocalRangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final VocalHistoryRepository vocalHistoryRepository;
    private final VocalRangeRepository vocalRangeRepository;
    private final SongBookmarkRepository songBookmarkRepository;
    private final ChartLikeRepository chartLikeRepository;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new IllegalArgumentException("존재하지 않는 회원: " + userId);
        vocalHistoryRepository.deleteByUserUserId(userId);
        vocalRangeRepository.deleteByUserUserId(userId);
        songBookmarkRepository.deleteByUserUserId(userId);
        chartLikeRepository.deleteByUserUserId(userId);
        userRepository.deleteById(userId);
    }
}

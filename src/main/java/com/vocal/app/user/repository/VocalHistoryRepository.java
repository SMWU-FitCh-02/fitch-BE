package com.vocal.app.user.repository;

import com.vocal.app.user.entity.VocalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VocalHistoryRepository extends JpaRepository<VocalHistory, Long> {
    List<VocalHistory> findByUserUserIdOrderByMeasuredAtDesc(Long userId);
    void deleteByUserUserId(Long userId);
}
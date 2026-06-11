package com.vocal.app.voice.repository;

import com.vocal.app.voice.entity.VocalRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VocalRangeRepository extends JpaRepository<VocalRange, Long> {
    Optional<VocalRange> findTopByUserUserIdOrderByMeasuredAtDesc(Long userId);
}

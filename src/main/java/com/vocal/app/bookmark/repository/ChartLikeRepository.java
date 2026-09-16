package com.vocal.app.bookmark.repository;

import com.vocal.app.bookmark.entity.ChartLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChartLikeRepository extends JpaRepository<ChartLike, Long> {
    List<ChartLike> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ChartLike> findByUserUserIdAndExternalId(Long userId, String externalId);
}

package com.vocal.app.bookmark.repository;

import com.vocal.app.bookmark.entity.SongBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SongBookmarkRepository extends JpaRepository<SongBookmark, Long> {
    List<SongBookmark> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    Optional<SongBookmark> findByUserUserIdAndSongSongId(Long userId, Long songId);
}

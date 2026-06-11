package com.vocal.app.song.repository;

import com.vocal.app.song.entity.SongFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SongFeatureRepository extends JpaRepository<SongFeature, Long> {
    Optional<SongFeature> findBySongSongId(Long songId);
}

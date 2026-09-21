package com.vocal.app.song.repository;

import com.vocal.app.song.entity.CrawledSongVocalRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrawledSongVocalRangeRepository extends JpaRepository<CrawledSongVocalRange, Long> {
    List<CrawledSongVocalRange> findBySongKeyIn(List<String> songKeys);
    Optional<CrawledSongVocalRange> findBySongKey(String songKey);
}
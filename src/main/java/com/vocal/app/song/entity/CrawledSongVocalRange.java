package com.vocal.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "crawled_song_vocal_ranges",
        uniqueConstraints = @UniqueConstraint(name = "UK_song_key", columnNames = "song_key")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrawledSongVocalRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    // title + "::" + artist 정규화 조합 — TJ 크롤링 곡과 매칭하는 키
    @Column(name = "song_key", nullable = false, unique = true)
    private String songKey;

    @Column(nullable = false)
    private Integer minNote; // Song.minNote와 동일한 MIDI 기준

    @Column(nullable = false)
    private Integer maxNote;

    public static String buildKey(String title, String artist) {
        return normalize(title) + "::" + normalize(artist);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
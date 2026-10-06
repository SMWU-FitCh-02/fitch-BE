package com.vocal.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 사용자가 "이 곡 분석해주세요"라고 요청한 곡. Mac에서 도는 분석 스크립트가
// PENDING 목록을 가져가서 분석한 뒤 결과를 올리면 DONE이 된다.
@Entity
@Table(
        name = "song_analysis_requests",
        uniqueConstraints = @UniqueConstraint(name = "UK_analysis_req_song_key", columnNames = "song_key")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SongAnalysisRequest {

    public enum Status { PENDING, DONE, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    // CrawledSongVocalRange.buildKey(title, artist)와 같은 키
    @Column(name = "song_key", nullable = false, unique = true)
    private String songKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
    }
}
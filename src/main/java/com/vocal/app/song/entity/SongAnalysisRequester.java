package com.vocal.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// "누가 어떤 곡을 분석 요청했는지" 기록. (같은 곡을 여러 사람이 요청할 수 있어서
// 곡별 요청 테이블(song_analysis_requests)과 분리했다.)
@Entity
@Table(
        name = "song_analysis_requesters",
        uniqueConstraints = @UniqueConstraint(name = "UK_requester_user_song", columnNames = {"username", "song_key"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SongAnalysisRequester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(name = "song_key", nullable = false)
    private String songKey;

    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
    }
}
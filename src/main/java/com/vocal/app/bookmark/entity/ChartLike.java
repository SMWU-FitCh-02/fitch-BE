package com.vocal.app.bookmark.entity;

import com.vocal.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chart_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "external_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChartLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "external_id", nullable = false)
    private String externalId; // Apple 차트 트랙 id 등 DB 밖 식별자

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(name = "artwork_url")
    private String artworkUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}

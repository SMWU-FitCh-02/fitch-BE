package com.vocal.app.voice.entity;

import com.vocal.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vocal_ranges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocalRange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private Integer minNote;
    @Column(nullable = false)
    private Integer maxNote;
    private Double stableScore;
    private LocalDateTime measuredAt;

    @PrePersist
    protected void onCreate() {
        measuredAt = LocalDateTime.now();
    }
}

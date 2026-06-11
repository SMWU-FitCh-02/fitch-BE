package com.vocal.app.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vocal_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime measuredAt;
    private Integer minNote;
    private Integer maxNote;
    private Double stableScore;

    @PrePersist
    protected void onCreate() {
        measuredAt = LocalDateTime.now();
    }
}

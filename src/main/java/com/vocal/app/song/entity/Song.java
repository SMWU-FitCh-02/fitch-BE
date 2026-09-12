package com.vocal.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;
    private String genre;

    @Column(name = "song_key")
    private String key;
    private Integer minNote;
    private Integer maxNote;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
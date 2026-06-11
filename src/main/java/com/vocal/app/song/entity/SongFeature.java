package com.vocal.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "song_features")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SongFeature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", unique = true, nullable = false)
    private Song song;
    @Column(columnDefinition = "TEXT") private String timbreVector;
    @Column(columnDefinition = "TEXT") private String mfccData;
}

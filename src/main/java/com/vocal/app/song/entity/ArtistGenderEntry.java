package com.vocal.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artist_genders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistGenderEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String artistName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtistGender gender;
}
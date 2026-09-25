package com.vocal.app.song.repository;

import com.vocal.app.song.entity.ArtistGenderEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistGenderRepository extends JpaRepository<ArtistGenderEntry, Long> {
    List<ArtistGenderEntry> findByArtistNameIn(List<String> artistNames);
    Optional<ArtistGenderEntry> findByArtistName(String artistName);
}
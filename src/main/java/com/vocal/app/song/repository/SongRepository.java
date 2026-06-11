package com.vocal.app.song.repository;

import com.vocal.app.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByMaxNoteLessThanEqual(int maxNote);
    List<Song> findByMaxNoteLessThanEqualAndArtistContaining(int maxNote, String artist);
    List<Song> findByMaxNoteLessThanEqualAndGenre(int maxNote, String genre);
}

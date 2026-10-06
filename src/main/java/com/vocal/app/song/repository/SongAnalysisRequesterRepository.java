package com.vocal.app.song.repository;

import com.vocal.app.song.entity.SongAnalysisRequester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongAnalysisRequesterRepository extends JpaRepository<SongAnalysisRequester, Long> {
    boolean existsByUsernameAndSongKey(String username, String songKey);

    List<SongAnalysisRequester> findByUsernameOrderByRequestedAtDesc(String username);
}
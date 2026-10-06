package com.vocal.app.song.repository;

import com.vocal.app.song.entity.SongAnalysisRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SongAnalysisRequestRepository extends JpaRepository<SongAnalysisRequest, Long> {
    Optional<SongAnalysisRequest> findBySongKey(String songKey);

    List<SongAnalysisRequest> findBySongKeyIn(Collection<String> songKeys);

    List<SongAnalysisRequest> findByStatusOrderByRequestedAtAsc(SongAnalysisRequest.Status status);
}
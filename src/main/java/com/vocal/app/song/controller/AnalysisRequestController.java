package com.vocal.app.song.controller;

import com.vocal.app.song.dto.SongKeyRequest;
import com.vocal.app.song.entity.CrawledSongVocalRange;
import com.vocal.app.song.entity.SongAnalysisRequest;
import com.vocal.app.song.entity.SongAnalysisRequest.Status;
import com.vocal.app.song.repository.CrawledSongVocalRangeRepository;
import com.vocal.app.song.repository.SongAnalysisRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// "분석 요청" 기능.
//  - POST /chart/analysis-requests          : 사용자가 곡 분석을 요청 (공개)
//  - POST /chart/analysis-requests/status   : 요청한 곡들의 진행 상태 조회 (공개)
//  - GET  /chart/analysis-requests/pending  : 분석 대기 목록 (분석 스크립트용, 로그인 필요)
//  - POST /chart/analysis-requests/fail     : 분석 실패 보고 (분석 스크립트용, 로그인 필요)
// 분석이 끝나면 /chart/vocal-ranges/bulk-upsert가 해당 요청을 DONE으로 바꿔준다.
@RestController
@RequestMapping("/chart/analysis-requests")
@RequiredArgsConstructor
public class AnalysisRequestController {

    private final SongAnalysisRequestRepository requestRepository;
    private final CrawledSongVocalRangeRepository rangeRepository;

    @PostMapping
    public Map<String, String> request(@RequestBody SongKeyRequest song) {
        String title = song.getTitle() == null ? "" : song.getTitle().trim();
        String artist = song.getArtist() == null ? "" : song.getArtist().trim();
        Map<String, String> res = new HashMap<>();
        if (title.isEmpty() || artist.isEmpty()) {
            res.put("status", "INVALID");
            return res;
        }

        String key = CrawledSongVocalRange.buildKey(title, artist);

        // 이미 분석된 곡이면 요청할 필요 없음
        if (rangeRepository.findBySongKey(key).isPresent()) {
            res.put("status", "DONE");
            return res;
        }

        Optional<SongAnalysisRequest> existing = requestRepository.findBySongKey(key);
        if (existing.isPresent()) {
            SongAnalysisRequest r = existing.get();
            if (r.getStatus() == Status.FAILED) { // 실패했던 곡은 다시 요청 가능
                r.setStatus(Status.PENDING);
                r.setRequestedAt(LocalDateTime.now());
                r.setCompletedAt(null);
                requestRepository.save(r);
            }
            res.put("status", r.getStatus().name());
            return res;
        }

        requestRepository.save(SongAnalysisRequest.builder()
                .title(title).artist(artist).songKey(key)
                .status(Status.PENDING)
                .build());
        res.put("status", Status.PENDING.name());
        return res;
    }

    // songKey -> PENDING / DONE / FAILED
    @PostMapping("/status")
    public Map<String, String> status(@RequestBody List<SongKeyRequest> songs) {
        List<String> keys = songs.stream()
                .map(s -> CrawledSongVocalRange.buildKey(s.getTitle(), s.getArtist()))
                .collect(Collectors.toList());
        Map<String, String> result = new HashMap<>();
        for (SongAnalysisRequest r : requestRepository.findBySongKeyIn(keys)) {
            result.put(r.getSongKey(), r.getStatus().name());
        }
        return result;
    }

    @GetMapping("/pending")
    public List<Map<String, String>> pending() {
        return requestRepository.findByStatusOrderByRequestedAtAsc(Status.PENDING).stream()
                .map(r -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("title", r.getTitle());
                    m.put("artist", r.getArtist());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/fail")
    public Map<String, Object> fail(@RequestBody List<SongKeyRequest> songs) {
        int count = 0;
        for (SongKeyRequest s : songs) {
            String key = CrawledSongVocalRange.buildKey(s.getTitle(), s.getArtist());
            Optional<SongAnalysisRequest> existing = requestRepository.findBySongKey(key);
            if (existing.isPresent()) {
                SongAnalysisRequest r = existing.get();
                r.setStatus(Status.FAILED);
                r.setCompletedAt(LocalDateTime.now());
                requestRepository.save(r);
                count++;
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("failed", count);
        return res;
    }
}
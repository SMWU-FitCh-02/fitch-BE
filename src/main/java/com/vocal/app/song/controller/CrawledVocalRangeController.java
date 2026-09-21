package com.vocal.app.song.controller;

import com.vocal.app.song.dto.CrawledVocalRangeResponse;
import com.vocal.app.song.dto.SongKeyRequest;
import com.vocal.app.song.dto.VocalRangeUpsertRequest;
import com.vocal.app.song.entity.CrawledSongVocalRange;
import com.vocal.app.song.repository.CrawledSongVocalRangeRepository;
import com.vocal.app.global.util.NoteUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chart/vocal-ranges")
@RequiredArgsConstructor
public class CrawledVocalRangeController {

    private final CrawledSongVocalRangeRepository repository;

    // TJ 차트 곡 (title, artist) 리스트를 보내면 songKey -> 음역대 맵으로 매칭해서 돌려줌
    @PostMapping
    public Map<String, CrawledVocalRangeResponse> getVocalRanges(@RequestBody List<SongKeyRequest> songs) {
        List<String> keys = songs.stream()
                .map(s -> CrawledSongVocalRange.buildKey(s.getTitle(), s.getArtist()))
                .collect(Collectors.toList());

        List<CrawledSongVocalRange> found = repository.findBySongKeyIn(keys);

        Map<String, CrawledVocalRangeResponse> result = new HashMap<>();
        for (CrawledSongVocalRange r : found) {
            result.put(r.getSongKey(), CrawledVocalRangeResponse.builder()
                    .minNote(r.getMinNote())
                    .maxNote(r.getMaxNote())
                    .minNoteLabel(NoteUtil.toLabel(r.getMinNote()))
                    .maxNoteLabel(NoteUtil.toLabel(r.getMaxNote()))
                    .build());
        }
        return result;
    }

    // 오프라인 오디오 분석 스크립트 결과를 일괄 등록/갱신 (관리용, AdminSongController 스타일)
    @PostMapping("/bulk-upsert")
    public Map<String, Object> bulkUpsert(@Valid @RequestBody List<VocalRangeUpsertRequest> items) {
        int inserted = 0;
        int updated = 0;
        for (VocalRangeUpsertRequest item : items) {
            String key = CrawledSongVocalRange.buildKey(item.getTitle(), item.getArtist());
            var existing = repository.findBySongKey(key);
            if (existing.isPresent()) {
                CrawledSongVocalRange e = existing.get();
                e.setMinNote(item.getMinNote());
                e.setMaxNote(item.getMaxNote());
                repository.save(e);
                updated++;
            } else {
                repository.save(CrawledSongVocalRange.builder()
                        .title(item.getTitle())
                        .artist(item.getArtist())
                        .songKey(key)
                        .minNote(item.getMinNote())
                        .maxNote(item.getMaxNote())
                        .build());
                inserted++;
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("inserted", inserted);
        summary.put("updated", updated);
        return summary;
    }
}
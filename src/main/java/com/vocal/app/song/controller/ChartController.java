package com.vocal.app.song.controller;

import com.vocal.app.song.entity.ArtistGenderEntry;
import com.vocal.app.song.dto.ArtistGenderUpsertRequest;
import com.vocal.app.song.repository.ArtistGenderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ArtistGenderRepository artistGenderRepository;

    @GetMapping("/artist-genders")
    public Map<String, String> getArtistGenders(@RequestParam List<String> artists) {
        List<ArtistGenderEntry> found = artistGenderRepository.findByArtistNameIn(artists);
        return found.stream()
                .collect(Collectors.toMap(
                        ArtistGenderEntry::getArtistName,
                        e -> e.getGender().name()
                ));
    }

    // 아티스트 성별 일괄 등록/갱신 (관리용, /chart/vocal-ranges/bulk-upsert 스타일)
    @PostMapping("/artist-genders/bulk-upsert")
    public Map<String, Object> bulkUpsert(@Valid @RequestBody List<ArtistGenderUpsertRequest> items) {
        int inserted = 0, updated = 0;
        for (ArtistGenderUpsertRequest item : items) {
            var existing = artistGenderRepository.findByArtistName(item.getArtistName());
            if (existing.isPresent()) {
                existing.get().setGender(item.getGender());
                artistGenderRepository.save(existing.get());
                updated++;
            } else {
                artistGenderRepository.save(ArtistGenderEntry.builder()
                        .artistName(item.getArtistName())
                        .gender(item.getGender())
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
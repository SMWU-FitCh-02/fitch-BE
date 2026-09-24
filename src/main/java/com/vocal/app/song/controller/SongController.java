package com.vocal.app.song.controller;

import com.vocal.app.song.dto.*;
import com.vocal.app.song.entity.Song;
import com.vocal.app.song.repository.SongRepository;
import com.vocal.app.song.service.SongService;
import com.vocal.app.voice.entity.VocalRange;
import com.vocal.app.voice.repository.VocalRangeRepository;
import com.vocal.app.global.util.NoteUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequiredArgsConstructor
public class SongController {
    private final SongService songService;
    private final SongRepository songRepository;
    private final VocalRangeRepository vocalRangeRepository;

    @PostMapping("/songs")
    public ResponseEntity<SongResponse> registerSong(@Valid @RequestBody SongRequest request) {
        return ResponseEntity.ok(songService.registerSong(request));
    }

    @GetMapping("/songs/{id}/vocal-range")
    public ResponseEntity<SongResponse> getSongVocalRange(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getVocalRange(id));
    }

    @GetMapping("/songs/{id}/key-adjust")
    public ResponseEntity<KeyAdjustResponse> keyAdjust(
        @PathVariable Long id,
        @RequestParam("user_id") Long userId) {
    Song song = songRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 곡: " + id));
    VocalRange range = vocalRangeRepository.findTopByUserUserIdOrderByMeasuredAtDesc(userId)
            .orElseThrow(() -> new IllegalStateException("음역대 측정 이력이 없습니다."));

    int songMin = song.getMinNote();
    int songMax = song.getMaxNote();
    int userMin = range.getMinNote();
    int userMax = range.getMaxNote();

    int adjust;
    if (songMin >= userMin && songMax <= userMax) {
        adjust = 0;  // 이미 사용자 음역대 안에 들어옴
    } else if (songMax > userMax && songMin >= userMin) {
        adjust = userMax - songMax;  // 최고음만 초과 -> 내리기
    } else if (songMin < userMin && songMax <= userMax) {
        adjust = userMin - songMin;  // 최저음만 부족 -> 올리기
    } else {
        // 곡 음역대 폭이 사용자보다 넓어서 완벽히 맞출 수 없는 경우 -> 중간값 기준 절충
        int songMid = (songMin + songMax) / 2;
        int userMid = (userMin + userMax) / 2;
        adjust = userMid - songMid;
    }

    return ResponseEntity.ok(KeyAdjustResponse.builder()
            .adjust(adjust)
            .description(adjust == 0 ? "키 조정 없음" : adjust > 0 ? adjust + "반음 올리기" : Math.abs(adjust) + "반음 낮추기")
            .originalKey(song.getKey())
            .adjustedKey(NoteUtil.adjustKey(song.getKey(), adjust)).build());
}

    @GetMapping("/songs")
    public ResponseEntity<List<SongResponse>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<SongResponse> getSong(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSong(id));
    }


}

package com.vocal.app.song.controller;

import com.vocal.app.song.dto.SongRequest;
import com.vocal.app.song.dto.SongResponse;
import com.vocal.app.song.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin/songs")
@RequiredArgsConstructor
public class AdminSongController {

    private final SongService songService;

    // 곡 등록
    @PostMapping
    public ResponseEntity<SongResponse> register(@Valid @RequestBody SongRequest request) {
        return ResponseEntity.ok(songService.registerSong(request));
    }

    // 곡 수정
    @PutMapping("/{id}")
    public ResponseEntity<SongResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody SongRequest request) {
        return ResponseEntity.ok(songService.updateSong(id, request));
    }

    // 곡 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }
}
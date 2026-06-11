package com.vocal.app.song.controller;

import com.vocal.app.song.dto.RecommendResponse;
import com.vocal.app.song.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


//사용자 음역대에 맞는 노래를 추천
@RestController @RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;

    @GetMapping("/recommend/{userId}")
    public ResponseEntity<RecommendResponse> recommend(
            @PathVariable Long userId,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String genre) {
        return ResponseEntity.ok(recommendService.recommend(userId, artist, genre));
    }
}

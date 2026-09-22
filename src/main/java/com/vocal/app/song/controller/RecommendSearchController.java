package com.vocal.app.song.controller;

import com.vocal.app.song.dto.RecommendSearchRequest;
import com.vocal.app.song.dto.RecommendSearchResponse;
import com.vocal.app.song.service.RecommendSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AI(자연어 질의)로 TJ차트 후보 곡 중에서 어울리는 곡을 골라주는 엔드포인트.
// DB에 의존하지 않고 프론트가 이미 들고 있는 TJ차트 후보 리스트를 그대로 받아서
// 그 안에서만 고르게 해, 존재하지 않는 곡을 추천하는 것을 방지한다.
@RestController
@RequiredArgsConstructor
public class RecommendSearchController {

    private final RecommendSearchService recommendSearchService;

    @PostMapping("/recommend/search")
    public ResponseEntity<RecommendSearchResponse> search(@RequestBody RecommendSearchRequest request) {
        var indices = recommendSearchService.searchSongs(request);
        return ResponseEntity.ok(new RecommendSearchResponse(indices));
    }
}
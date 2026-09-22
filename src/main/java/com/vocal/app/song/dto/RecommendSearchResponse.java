package com.vocal.app.song.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecommendSearchResponse {
    // candidates 리스트에서 0-based 인덱스로, AI가 골라준 곡들
    private List<Integer> matchedIndices;
}
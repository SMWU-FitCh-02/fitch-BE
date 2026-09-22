package com.vocal.app.song.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendSearchRequest {
    private String query;
    private List<CandidateDto> candidates;

    @Data
    public static class CandidateDto {
        private String title;
        private String artist;
    }
}
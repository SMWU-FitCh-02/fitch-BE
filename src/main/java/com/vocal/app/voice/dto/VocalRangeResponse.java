package com.vocal.app.voice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class VocalRangeResponse {
    private Long userId;
    private Integer minNote;
    private Integer maxNote;
    private Double stableScore;
    private String minNoteLabel;
    private String maxNoteLabel;
    private LocalDateTime measuredAt;
}

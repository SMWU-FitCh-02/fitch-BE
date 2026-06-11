package com.vocal.app.voice.dto;

import lombok.*;

@Getter
@Setter
public class VoiceAnalysisResult {
    private Integer minNote;
    private Integer maxNote;
    private Double stableScore;
}

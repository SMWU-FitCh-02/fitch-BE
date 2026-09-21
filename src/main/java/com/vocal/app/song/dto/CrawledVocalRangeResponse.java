package com.vocal.app.song.dto;

import lombok.*;

@Getter @Builder
public class CrawledVocalRangeResponse {
    private Integer minNote;
    private Integer maxNote;
    private String minNoteLabel;
    private String maxNoteLabel;
}
package com.vocal.app.song.dto;

import lombok.*;

@Getter @Builder
public class SongResponse {
    private Long songId;
    private String title;
    private String artist;
    private String genre;
    private String key;
    private Integer minNote;
    private Integer maxNote;
    private String minNoteLabel;
    private String maxNoteLabel;
}

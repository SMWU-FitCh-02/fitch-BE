package com.vocal.app.song.dto;

import com.vocal.app.song.entity.ArtistGender;
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
    private ArtistGender artistGender;
}
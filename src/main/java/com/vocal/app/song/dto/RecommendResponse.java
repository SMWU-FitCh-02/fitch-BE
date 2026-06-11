package com.vocal.app.song.dto;

import lombok.*;
import java.util.List;

@Getter @Builder
public class RecommendResponse {
    private Long userId;
    private int userMaxNote;
    private String userMaxNoteLabel;
    private List<SongResponse> recommendedSongs;
}

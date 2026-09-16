package com.vocal.app.bookmark.dto;

import lombok.*;

@Getter @Builder
public class ChartLikeResponse {
    private String externalId;
    private String title;
    private String artist;
    private String artworkUrl;
}

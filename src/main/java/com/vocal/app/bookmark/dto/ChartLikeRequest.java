package com.vocal.app.bookmark.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
public class ChartLikeRequest {
    @NotBlank private String externalId;
    @NotBlank private String title;
    @NotBlank private String artist;
    private String artworkUrl;
}

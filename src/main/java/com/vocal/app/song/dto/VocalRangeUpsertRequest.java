package com.vocal.app.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
public class VocalRangeUpsertRequest {
    @NotBlank private String title;
    @NotBlank private String artist;
    @NotNull private Integer minNote;
    @NotNull private Integer maxNote;
}
package com.vocal.app.song.dto;

import com.vocal.app.song.entity.ArtistGender;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class SongRequest {
    @NotBlank private String title;
    @NotBlank private String artist;
    private String genre;
    private String key;
    @NotNull private Integer minNote;
    @NotNull private Integer maxNote;
    private ArtistGender artistGender;
}
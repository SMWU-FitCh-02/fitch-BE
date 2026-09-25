package com.vocal.app.song.dto;

import com.vocal.app.song.entity.ArtistGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ArtistGenderUpsertRequest {
    @NotBlank
    private String artistName;

    @NotNull
    private ArtistGender gender;
}
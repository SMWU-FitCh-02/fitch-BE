package com.vocal.app.song.dto;

import lombok.*;

@Getter @Builder
public class KeyAdjustResponse {
    private int adjust;
    private String description;
    private String originalKey;
    private String adjustedKey;
}

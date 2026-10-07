package com.vocal.app.voice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VocalRangeSaveRequest {
    private Integer minNote;
    private Integer maxNote;
}

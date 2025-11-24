package dev.benew.filmbiblecrawling.dto;

import lombok.Data;

@Data
public class ThumbDto {

    private Long vtIdx;
    private String videoId;
    private String thumbUrl;
    private String thumbType;
}

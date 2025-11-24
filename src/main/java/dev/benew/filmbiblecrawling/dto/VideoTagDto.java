package dev.benew.filmbiblecrawling.dto;

import lombok.Data;

@Data
public class VideoTagDto {
    private String videoId;
    private String tagName;
    private String tagOrder;
}

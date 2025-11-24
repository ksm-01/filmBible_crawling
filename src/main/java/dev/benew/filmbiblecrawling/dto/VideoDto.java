package dev.benew.filmbiblecrawling.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class VideoDto {
    private String videoId;
    private String videoTitle;
    private String videoThumb;
    private String videoFullPath;
    private String videoType;
    private LocalTime videoPlayTime;
    private Long videoView;
    private Long videoLike;

    private String recommendType;
    private String artistName;
    private String category;
    private String genre;
    private String mood;

    private LocalDateTime videoCreatedDate;
    private LocalDateTime videoRegDate;
}

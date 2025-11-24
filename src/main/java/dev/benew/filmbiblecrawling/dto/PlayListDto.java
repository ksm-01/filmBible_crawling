package dev.benew.filmbiblecrawling.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class PlayListDto {
    private Long playListIdx;
    private String playListId;
    private String playListTitle;
    private String playListThumb;
    private String playListFullPath;
    private LocalDateTime regDate;
}

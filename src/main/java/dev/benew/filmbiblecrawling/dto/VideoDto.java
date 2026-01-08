package dev.benew.filmbiblecrawling.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class VideoDto {
   private Long videoIdx;
   private String videoId;
   private String videoTitle;
   private String videoThumb;
   private String videoUrl;
   private String videoType;
   private LocalTime playTime;

   private Long videoView;
   private Boolean viewDisable;
   private Long videoLike;
   private Boolean likeDisable;
   private Long videoPosition;
   private String videoStatus;
   private String embedUrl;

   private LocalDateTime videoCreateDate;
   private LocalDateTime videoRegDate;
   private LocalDateTime videoRefreshDate;



}

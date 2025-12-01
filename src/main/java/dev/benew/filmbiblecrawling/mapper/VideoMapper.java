package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.VideoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface VideoMapper {


    List<VideoDto> findAllVideo();

    List<String> findAllVideoId();

    VideoDto findByVideoId(@Param("videoId") String videoId);

    Boolean saveVideo(VideoDto videoDto);

    Boolean updateVideo(VideoDto videoDto);

//    Boolean updateVideoDetailInfo(
//            @Param("id") String id,
//            @Param("playTime") LocalTime playTime,
//            @Param("createdDate") LocalDateTime createdDate,
//            @Param("viewDisable") Boolean viewDisable,
//            @Param("view") Long view,
//            @Param("likeDisable") Boolean likeDisable,
//            @Param("like") Long like
//    );

    Boolean updateVideoDetailInfo(
            VideoDto videoDto
    );
}

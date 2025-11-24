package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.VideoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMapper {


    List<VideoDto> findAllVideo();

    VideoDto findByVideoId(@Param("videoId") String videoId);

    Boolean saveVideo(VideoDto videoDto);

    Boolean updateVideoDetailInfo(
            @Param("videoId") String videoId,
            @Param("view") Long view,
            @Param("like") Long like
    );
}

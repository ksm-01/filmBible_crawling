package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.ThumbDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ThumbMapper {

    ThumbDto findThumbById(
            @Param("videoId") String videoId
    );

    Boolean saveVideoThumb(
            @Param("thumbList")List<ThumbDto> thumbList
    );

    Boolean deleteThumb(
            @Param("videoId") String videoId
    );
}

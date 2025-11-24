package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.ThumbDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ThumbMapper {

    Boolean saveVideoThumb(
            @Param("thumbList")List<ThumbDto> thumbList
    );
}

package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.ThumbDto;
import dev.benew.filmbiblecrawling.dto.VideoTagDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    List<String> findAllVideoIdsByTag();

    VideoTagDto findTagById(
            @Param("videoId") String videoId
    );

    Boolean saveTag(
            @Param("videoId") String videoId,
            @Param("tagList") List<String> tagList
    );

    Boolean deleteTag(
            @Param("videoId") String videoId
    );
}

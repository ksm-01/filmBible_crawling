package dev.benew.filmbiblecrawling.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayListHasVideoMapper {

    Integer countPlayListHasVideo(
            @Param("playListId") String playListId,
            @Param("videoId") String videoId
    );

    Boolean savePlayListHasVideo(
            @Param("playListId") String playListId,
            @Param("videoId") String videoId
    );
}

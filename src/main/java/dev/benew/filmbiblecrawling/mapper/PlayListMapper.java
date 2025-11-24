package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.PlayListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlayListMapper {

    PlayListDto findByPlayListId(
            @Param("playListId") String playListId
    );

    List<String> findAllPlayListId();

    Boolean savePlayList(PlayListDto playListDto);
}

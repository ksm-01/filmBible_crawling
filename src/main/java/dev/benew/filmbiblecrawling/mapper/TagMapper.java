package dev.benew.filmbiblecrawling.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    Boolean saveTag(
            @Param("videoId") String videoId,
            @Param("tagList") List<String> tagList
    );
}

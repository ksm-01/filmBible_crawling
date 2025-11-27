package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.ChannelDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChannelMapper {

    ChannelDto findChannelById(
            @Param("channelId") String channelId
    );

    List<String> findAllChannelId();

    Boolean saveChannel(ChannelDto channelDto);

    Boolean updateChannel(ChannelDto channelDto);
}

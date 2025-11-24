package dev.benew.filmbiblecrawling.mapper;

import dev.benew.filmbiblecrawling.dto.ChannelDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChannelMapper {

    Boolean saveChannel(ChannelDto channelDto);
}

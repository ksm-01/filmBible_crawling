package dev.benew.filmbiblecrawling.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import dev.benew.filmbiblecrawling.dto.ChannelDto;
import dev.benew.filmbiblecrawling.mapper.ChannelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChannelService {

    private final YoutubeApiService youtubeApiService;
    private final ChannelMapper channelMapper;

    public ChannelService(YoutubeApiService youtubeApiService, ChannelMapper channelMapper) {
        this.youtubeApiService = youtubeApiService;
        this.channelMapper = channelMapper;
    }

     public void updateChannelInfo() throws GeneralSecurityException, IOException {
        ChannelListResponse response =  youtubeApiService.channelApi();
        Channel channel = response.getItems().get(0);
        ChannelDto channelDto = this.convertChnnelDto(channel);
        channelMapper.updateChannel(channelDto);
     }


//    public void upsertChannelInfo() throws GeneralSecurityException, IOException {
//        Set<String> channelIdSet = new HashSet<>(channelMapper.findAllChannelId());
//
//        ChannelListResponse response = youtubeApiService.channelApi();
//
//        for (Channel channel : response.getItems()) {
//            ChannelDto channelDto = this.convertChnnelDto(channel);
//            // 채널명, 구독자 업데이트
//            if (channelIdSet.contains(channelDto.getChannelId())) {
//                channelMapper.updateChannel(channelDto);
//            } else {
//                channelMapper.saveChannel(channelDto);
//                channelIdSet.add(channelDto.getChannelId());
//            }
//        }
//    }

    protected ChannelDto convertChnnelDto(Channel channel) {
        ChannelSnippet snippet = channel.getSnippet();
        ChannelStatistics statistics = channel.getStatistics();

        String channelId = channel.getId();
        String channelName = snippet.getTitle();
//        String channelDescription = snippet.getDescription(); // 채널 설명

        // 채널 생성 날짜
        DateTime dateTime = snippet.getPublishedAt();
        LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        Long subCount = statistics.getSubscriberCount().longValue();

        return ChannelDto.builder()
                .channelId(channelId)
                .channelName(channelName)
                .channelSubscriber(subCount)
                .createdDate(createdDate)
                .build();
    }

}

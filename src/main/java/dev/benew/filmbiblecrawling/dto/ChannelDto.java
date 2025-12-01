package dev.benew.filmbiblecrawling.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChannelDto {
    private String channelId;
    private String channelName;
    private Long channelSubscriber;

    private LocalDateTime createdDate;
    private LocalDateTime channelRegDate;
    private LocalDateTime refreshDate;
}

package dev.benew.filmbiblecrawling.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChannelDto {
    private String channelId;
    private String channelName;
    private Integer channelSubscriber;
    private LocalDateTime channelRegDate;
}

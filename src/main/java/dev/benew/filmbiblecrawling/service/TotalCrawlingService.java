package dev.benew.filmbiblecrawling.service;

import dev.benew.filmbiblecrawling.service.video.UpdateVideoInfoService;
import dev.benew.filmbiblecrawling.service.video.VideoService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class TotalCrawlingService {

    private final YoutubeApiService youtubeApiService;
    private final ChannelService channelService;
    private final PlayListService playListService;
    private final VideoService videoService;
    private final UpdateVideoInfoService videoInfoService;
    private final ThumbnailAndTagService thumbAndTagService;


    public TotalCrawlingService(PlayListService playListService, VideoService videoService, YoutubeApiService youtubeApiService, ChannelService channelService, UpdateVideoInfoService videoInfoService, ThumbnailAndTagService thumbAndTagService) {
        this.playListService = playListService;
        this.videoService = videoService;
        this.youtubeApiService = youtubeApiService;
        this.channelService = channelService;
        this.videoInfoService = videoInfoService;
        this.thumbAndTagService = thumbAndTagService;
    }

    public void crawlingTotal() throws GeneralSecurityException, IOException {


        // 채널 정보 저장, 업데이트(채널명,구독자 수)
//        channelService.upsertChannelInfo();
        channelService.updateChannelInfo();

        // 재생목록 저장, 업데이트(제목,썸네일,동영상 개수, 노출상태)
        playListService.upsertPlayList();

        // 재생목록의 영상들 저장,업데이트(제목,순서,노출상태)
        videoService.upsertVideo();

        // 숏츠 저장,업데이트(제목,순서,노출상태) -> 재생목록에 video로 들어온 shorts도 type 업데이트
        videoService.upsertOnlyShorts();

        // 재생시간, 등록일, 조회수, 좋아요, 업데이트 날짜
        videoInfoService.updateVideoInfo();

        // 비디오 썸네일(default, medium, high, standard, maxres), 태그 저장,업데이트
        thumbAndTagService.thumbAndTagMapping();
    }
}

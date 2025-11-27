package dev.benew.filmbiblecrawling.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class TotalCrawlingService {

    private final YoutubeApiService youtubeApiService;

    public TotalCrawlingService(YoutubeApiService youtubeApiService) {
        this.youtubeApiService = youtubeApiService;
    }

    public void crawlingTotal() throws GeneralSecurityException, IOException {

        
        // 채널 정보 저장, 업데이트(채널명,구독자 수)
        youtubeApiService.upsertChannelInfo();
        
        // 재생목록 저장, 업데이트(제목,썸네일,동영상 개수, 노출상태)
        youtubeApiService.upsertPlayList();

        // 재생목록의 영상들 저장,업데이트(제목,순서,노출상태)
        youtubeApiService.upsertVideo();

        // 숏츠 저장,업데이트(제목,순서,노출상태)
        youtubeApiService.saveShorts();

        // 재생시간, 조회수, 좋아요
        youtubeApiService.updateVideoInfo();

        // 비디오 썸네일(default, medium, high, standard, maxres), 태그 저장,업데이트
        youtubeApiService.updateThumbAndTag();
    }
}

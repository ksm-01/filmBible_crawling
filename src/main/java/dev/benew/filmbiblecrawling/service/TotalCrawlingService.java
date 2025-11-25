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

        // 재생목록 저장
        youtubeApiService.searchPlayList();

        // 재생목록의 영상들 저장
        youtubeApiService.newVideoCrawler();

        // 숏츠만 가져오기
        youtubeApiService.saveShorts();

        // 좋아요, 조회수, 재생시간 저장
        youtubeApiService.likeAndView();

        // 비디오 썸네일 사이즈타입별 저장, 태그 저장
        youtubeApiService.saveThumb();
    }
}

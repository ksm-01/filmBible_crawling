package dev.benew.filmbiblecrawling;

import dev.benew.filmbiblecrawling.service.*;
import dev.benew.filmbiblecrawling.service.video.UpdateVideoInfoService;
import dev.benew.filmbiblecrawling.service.video.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@SpringBootTest
public class VideoCrawlingTest {

    @Autowired
    YoutubeApiService youtubeApiService;

    @Autowired
    PlayListService playListService;

    @Autowired
    VideoService videoService;

    @Autowired
    UpdateVideoInfoService videoInfoService;

    @Autowired
    ChannelService channelService;

//    // 채널 정보
    @Test
    void channelInfo() throws GeneralSecurityException, IOException {
        channelService.updateChannelInfo();
    }


//    // 재생 목록 가져와서 저장하기
    @Test
    void findPlayList() throws GeneralSecurityException, IOException {
        playListService.upsertPlayList();
    }

//    // 재생목록 비디오 저장
    @Test
    void saveVideo() throws GeneralSecurityException, IOException {
        videoService.upsertVideo();
    }

//    // 숏츠 영상들만 가져오기
    @Test
    void shorts() throws GeneralSecurityException, IOException {
        videoService.upsertOnlyShorts();
    }

    // 좋아요 조회수, 재생시간 저장
    @Test
    void updateVideo() throws GeneralSecurityException, IOException {
        videoInfoService.updateVideoInfo();
    }

    @Test
    void categories() throws GeneralSecurityException, IOException {
        videoService.getCateInfo();
    }

    // 썸네일, 태그 저장
//    @Test
//    void saveThumb() throws GeneralSecurityException, IOException {
//        youtubeApiService.updateThumbAndTag();
//    }

//
//    // 채널 정보 가져오기
//    @Test
//    void findChannel() throws GeneralSecurityException, IOException {
//        youtubeApiService.searchChannel("필름바이블");
//    }




}

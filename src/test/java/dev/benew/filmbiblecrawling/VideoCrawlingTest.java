package dev.benew.filmbiblecrawling;

import dev.benew.filmbiblecrawling.service.YoutubeApiService;
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

    // 재생 목록 가져와서 저장하기
    @Test
    void findPlayList() throws GeneralSecurityException, IOException {
        youtubeApiService.searchPlayList();
    }

    // 재생목록 비디오 저장
    @Test
    void saveVideo() throws GeneralSecurityException, IOException {
        youtubeApiService.newVideoCrawler();
    }

    @Test
    void saveShorts() throws GeneralSecurityException, IOException {
        youtubeApiService.saveShorts();
    }

    // 좋아요 조회수, 재생시간 저장
    @Test
    void updateVideo() throws GeneralSecurityException, IOException {
        youtubeApiService.likeAndView();
    }

    // 썸네일, 태그 저장
    @Test
    void saveThumb() throws GeneralSecurityException, IOException {
        youtubeApiService.saveThumb();
    }


    @Test
    void channlInfo() throws GeneralSecurityException, IOException {
        youtubeApiService.channelInfo();
    }


    // 채널 정보 가져오기
    @Test
    void findChannel() throws GeneralSecurityException, IOException {
        youtubeApiService.searchChannel("필름바이블");
    }


//   @Test
//    void shorts() throws GeneralSecurityException, IOException {
//        youtubeApiService.test();
//    }

}

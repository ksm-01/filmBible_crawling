package dev.benew.filmbiblecrawling;

import dev.benew.filmbiblecrawling.service.YoutubeApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthStateCacheable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@SpringBootTest
class FilmbibleCrawlingApplicationTests {

    @Autowired
    YoutubeApiService youtubeApiService;

    @Test
    void contextLoads() {
    }

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

    // 채널 정보 가져오기
    @Test
    void findChannel() throws GeneralSecurityException, IOException {
        youtubeApiService.searchChannel("필름바이블");
    }

//    @Test
//    void updateVideo() throws GeneralSecurityException, IOException {
//        youtubeApiService.likeAndView();
//    }

    @Test
    void shorts() throws GeneralSecurityException, IOException {
        youtubeApiService.test();
    }
}

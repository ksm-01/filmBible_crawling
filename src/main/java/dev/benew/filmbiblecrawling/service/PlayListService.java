package dev.benew.filmbiblecrawling.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.dto.PlayListDto;
import dev.benew.filmbiblecrawling.dto.VideoDto;
import dev.benew.filmbiblecrawling.mapper.PlayListMapper;
import dev.benew.filmbiblecrawling.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Service
public class PlayListService {

    private final YoutubeApiService youtubeApiService;
    private final PlayListMapper playListMapper;

    public PlayListService(YoutubeApiService youtubeApiService, PlayListMapper playListMapper) {
        this.youtubeApiService = youtubeApiService;
        this.playListMapper = playListMapper;
    }

    public void upsertPlayList() throws GeneralSecurityException, IOException {

        System.out.println("재생목록 시작 -----------------------------------");

        Set<String> playListIdSet = new HashSet<>(playListMapper.findAllPlayListId());

        PlaylistListResponse response = youtubeApiService.playListApi();

        for (Playlist playlistItem : response.getItems()) {
            PlayListDto playListDto = this.convertPlayListDto(playlistItem);

            if (playListIdSet.contains(playListDto.getPlayListId())) {
                playListMapper.updatePlayList(playListDto);  // 제목, 썸네일, 동영상 수, 노출 상태 업데이트
            } else {
                playListMapper.savePlayList(playListDto);
                playListIdSet.add(playListDto.getPlayListId());  // 처음에 가져온 id 최신화
                System.out.println("새로 저장된 재생목록 ID: " + playListDto.getPlayListId());
            }
        }
    }


    protected PlayListDto convertPlayListDto(Playlist item) {
        PlaylistSnippet snippet = item.getSnippet();
        PlaylistContentDetails contentDetails = item.getContentDetails();
        PlaylistStatus status = item.getStatus();

        String playListId = item.getId();

        DateTime dateTime = snippet.getPublishedAt();  // 재생목록 생성된 날짜
        // 재생목록 생성날짜 LocalDateTime으로 변환
        LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        String playListTitle = snippet.getTitle();
//        String playListDescription = snippet.getDescription();  // 재생목록 설명
//        System.out.println("playListDescription: " + playListDescription);

        String playListThumb = null;

        // 썸네일 있는거로 가져오기
        if (snippet.getThumbnails().getHigh() != null) {
            playListThumb = snippet.getThumbnails().getHigh().getUrl();
        } else if (snippet.getThumbnails().getMedium() != null) {
            playListThumb = snippet.getThumbnails().getMedium().getUrl();
        } else if (snippet.getThumbnails().getDefault() != null) {
            playListThumb = snippet.getThumbnails().getDefault().getUrl();
        }

        String playlistUrl = "https://www.youtube.com/playlist?list=" + playListId;
        String show = status.getPrivacyStatus();  // 재생목록 공개상태
//        System.out.println("show :" + show);

        // 재생목록에 있는 동영상 개수
        Long videoCount = contentDetails.getItemCount();
//        System.out.println("vicdeoCount:" + videoCount);


        // 문자열 깨짐 방지
        String normalized_string = Normalizer.normalize(playListTitle, Normalizer.Form.NFC);

        return PlayListDto.builder()
                .playListId(playListId)
                .playListTitle(normalized_string)
                .playListThumb(playListThumb)
                .playListFullPath(playlistUrl)
                .createdDate(createdDate)
                .videoCount(videoCount)
                .playListStatus(show)
                .build();
    }

}

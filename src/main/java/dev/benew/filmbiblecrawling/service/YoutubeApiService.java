package dev.benew.filmbiblecrawling.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.mapper.*;
import dev.benew.filmbiblecrawling.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class YoutubeApiService {

    @Value("${youtube.api-key}")
    private String youtubeApiKey;
    @Value("${youtube.application-name}")
    private String youtubeAppName;
    @Value("${youtube.channel-id}")
    private String channelId;
    @Value("${youtube.uploadVideo-id}")
    private String uploadVideoId;    // 채널 전체 동영상 재생목록 id


    private YouTube initYoutube() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null
        )
                .setApplicationName(youtubeAppName)
                .build();
    }

    // 채널 찾기
    public void searchChannel(
            String query
    ) throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        SearchListResponse res = youtube.search()
                .list("snippet")
                .setType("channel")
                .setKey(youtubeApiKey)
                .setQ(query) // 핸들 혹은 채널명으로 검색
                .execute();

        log.info("res:");
        JsonUtil.prettyString(res);
    }

    // 채널정보 가져오기
    public ChannelListResponse channelApi() throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        YouTube.Channels.List req = youtube.channels()
                .list("id, snippet, statistics")
                .setId("UCijh44bpBtXO52xkLhb9vUQ")
                .setKey(youtubeApiKey);

        return req.execute();
    }

    // 재생목록 가져오기
    public PlaylistListResponse playListApi() throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        YouTube.Playlists.List request = youtube.playlists()
                .list("id,snippet, status, contentDetails")
                .setKey(youtubeApiKey)
                .setChannelId(channelId)
                .setMaxResults(50L);

        return request.execute();
    }


    // 재생목록 동영상 가져오기
    public List<PlaylistItem> playListItemApi(String playListId) throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();
        List<PlaylistItem> allItems = new ArrayList<>();

        // 전체 동영상 재생목록 id
        String searchId = uploadVideoId;

        if (playListId != null) {
            searchId = playListId;
        }

        YouTube.PlaylistItems.List request = youtube.playlistItems()
                .list("id,snippet,contentDetails, status")
                .setKey(youtubeApiKey)
                .setPlaylistId(searchId)
                .setMaxResults(50L);

        String nextPageToken = null;
        do {
            if (nextPageToken != null)
                request.setPageToken(nextPageToken);

            PlaylistItemListResponse response = request.execute();
            allItems.addAll(response.getItems());
            nextPageToken = response.getNextPageToken();

        } while (nextPageToken != null);

        return allItems;
    }

    // 비디오 정보 가져오기
    public VideoListResponse videoApi(List<String> videoIds) throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        YouTube.Videos.List req = youtube.videos()
                .list("snippet,contentDetails,statistics")
                .setId(String.join(",", videoIds))
                .setKey(youtubeApiKey);
        return req.execute();
    }

}

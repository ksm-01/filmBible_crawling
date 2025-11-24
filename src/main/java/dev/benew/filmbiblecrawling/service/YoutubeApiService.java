package dev.benew.filmbiblecrawling.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.dto.ChannelDto;
import dev.benew.filmbiblecrawling.dto.PlayListDto;
import dev.benew.filmbiblecrawling.dto.VideoDto;
import dev.benew.filmbiblecrawling.mapper.PlayListHasVideoMapper;
import dev.benew.filmbiblecrawling.mapper.PlayListMapper;
import dev.benew.filmbiblecrawling.mapper.VideoMapper;
import dev.benew.filmbiblecrawling.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.time.*;
import java.util.List;
import java.util.Map;
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


    private final PlayListMapper playListMapper;
    private final VideoMapper videoMapper;
    private final PlayListHasVideoMapper phvMapper;

    public YoutubeApiService(PlayListMapper playListMapper, VideoMapper videoMapper, PlayListHasVideoMapper phvMapper) {
        this.playListMapper = playListMapper;
        this.videoMapper = videoMapper;
        this.phvMapper = phvMapper;
    }

    private YouTube initYoutube() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null
        )
                .setApplicationName(youtubeAppName)
                .build();
    }

    // 재생목록 가져오기
    public void searchPlayList() throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        YouTube.Playlists.List request = youtube.playlists()
                .list("id,snippet")
                .setKey(youtubeApiKey)
                .setChannelId("tVdj9p4PKfc")
                .setMaxResults(50L);

        PlaylistListResponse response = request.execute();

        for (Playlist playlist : response.getItems()) {
            String playlistId = playlist.getId();
            String title = playlist.getSnippet().getTitle();

            String thumb = "";

            // 썸네일
            PlaylistSnippet snippet = playlist.getSnippet();

            System.out.println("shorts:" + snippet);

            if (snippet.getThumbnails() == null) {
                continue;
            }

            // 썸네일 있는거로 가져오기
            if (snippet.getThumbnails().getHigh() != null) {
                thumb = snippet.getThumbnails().getHigh().getUrl();
            } else if (snippet.getThumbnails().getMedium() != null) {
                thumb = snippet.getThumbnails().getMedium().getUrl();
            } else {
                thumb = snippet.getThumbnails().getDefault().getUrl();
            }


            String playlistUrl = "https://www.youtube.com/playlist?list=" + playlist.getId();


            if(playListMapper.findByPlayListId(playlistId) != null){
                continue;
            }

            System.out.println("Playlist ID: " + playlistId);
            System.out.println("Title: " + title);

            PlayListDto dto = PlayListDto.builder()
                    .playListId(playlistId)
                    .playListTitle(title)
                    .playListThumb(thumb)
                    .playListFullPath(playlistUrl)
                    .build();

            // 문자열 깨짐 방지
            String normalized_string = Normalizer.normalize(dto.getPlayListTitle(), Normalizer.Form.NFC);
            dto.setPlayListTitle(normalized_string);

            playListMapper.savePlayList(dto);
       }
    }

    public void test() throws GeneralSecurityException, IOException {

        YouTube youtube = this.initYoutube();

        YouTube.Videos.List request = youtube.videos()
                .list("snippet,contentDetails,statistics")
                .setId("RKi98sWtwGA")
                .setKey(youtubeApiKey);

        VideoListResponse response = request.execute();

        JsonUtil.prettyString(response.getItems());

        for (Video video : response.getItems()) {
            String videoId = video.getId();
            String title = video.getSnippet().getTitle();
            String thumb = video.getSnippet().getThumbnails().getHigh().getUrl();
            String fullPath = "https://www.youtube.com/shorts/" + videoId;
            String playTime = video.getContentDetails().getDuration(); // 영상 길이
            Long viewCount = video.getStatistics().getViewCount().longValue();
            Long likeCount = video.getStatistics().getLikeCount().longValue();
            DateTime dateTime = video.getSnippet().getPublishedAt();

            LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            Duration duration = Duration.parse(playTime);
            LocalTime videoTime = LocalTime.ofSecondOfDay(duration.getSeconds());


            VideoDto videoDto = VideoDto.builder()
                    .videoId(videoId)
                    .videoTitle(title)
                    .videoThumb(thumb)
                    .videoFullPath(fullPath)
                    .videoPlayTime(videoTime)
                    .videoView(viewCount)
                    .videoLike(likeCount)
                    .videoCreatedDate(createdDate)
                    .videoType("shorts")
                    .build();

            // 문자열 깨짐 방지
            String normalized_string = Normalizer.normalize(videoDto.getVideoTitle(), Normalizer.Form.NFC);
            videoDto.setVideoTitle(normalized_string);

            videoMapper.saveVideo(videoDto);

            JsonUtil.prettyString(video);
        }





    }


    public void newVideoCrawler() throws GeneralSecurityException, IOException {

        YouTube youtube = this.initYoutube();

        List<String> idList = playListMapper.findAllPlayListId();

        for (String playlistId : idList) {

            YouTube.PlaylistItems.List request = youtube.playlistItems()
                    .list("id,snippet,contentDetails")
                    .setKey(youtubeApiKey)
                    .setPlaylistId(playlistId)
                    .setMaxResults(50L);

            PlaylistItemListResponse response = request.execute();

            for (PlaylistItem item : response.getItems()) {
                String videoId = item.getContentDetails().getVideoId();
                String title = item.getSnippet().getTitle();
                String thumb = "";
                if (item.getSnippet().getThumbnails() == null) {
                    continue;
                }

                if (item.getSnippet().getThumbnails().getHigh() != null) {
                    thumb = item.getSnippet().getThumbnails().getHigh().getUrl();
                } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                    thumb = item.getSnippet().getThumbnails().getMedium().getUrl();
                } else if (item.getSnippet().getThumbnails().getDefault() != null){
                    thumb = item.getSnippet().getThumbnails().getDefault().getUrl();
                }

                String playlistUrl = "https://www.youtube.com/watch?v=" + videoId;

                LocalTime playTime = LocalTime.parse(item.getSnippet().getDescription());
                DateTime dateTime = item.getContentDetails().getVideoPublishedAt();

                LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                if (videoMapper.findByVideoId(videoId) != null) {
                    continue;
                }

                VideoDto videoDto = VideoDto.builder()
                        .videoId(videoId)
                        .videoTitle(title)
                        .videoThumb(thumb)
                        .videoFullPath(playlistUrl)
                        .videoPlayTime(playTime)
                        .videoCreatedDate(createdDate)
                        .build();

                System.out.println("Video ID: " + videoId);


                // 제목 깨짐 방지
                String normalized_string = Normalizer.normalize(videoDto.getVideoTitle(), Normalizer.Form.NFC);
                videoDto.setVideoTitle(normalized_string);

                videoMapper.saveVideo(videoDto);

                if (phvMapper.countPlayListHasVideo(playlistId, videoId) == 0) {
                    phvMapper.savePlayListHasVideo(playlistId, videoId);
                }

            }
        }

    }


    // 좋아요, 조회수
//   public void likeAndView() throws GeneralSecurityException, IOException {
//        YouTube youtube = this.initYoutube();
//
//        List<VideoDto> videoList = videoMapper.findAllVideo();
//        List<String> videoIdList = videoList.stream()
//                .map(VideoDto::getVideoId)
//                .toList();
//
//       YouTube.Videos.List request = youtube.videos()
//               .list("snippet,contentDetails,statistics")
//               .setId(String.join(",", videoIdList))
//               .setKey(youtubeApiKey)
//               .setFields("items(id,snippet(publishedAt),contentDetails(duration),statistics(viewCount,likeCount))");
//
//
//       VideoListResponse response = request.execute();
//
//       Map<String, Video> videoMapById = response.getItems().stream().collect(Collectors.toMap(
//               Video::getId,
//               it -> it
//       ));
//
//       videoMapById.forEach((videoId, video) -> {
//                   VideoStatistics statistics = video.getStatistics();
//                   VideoContentDetails details = video.getContentDetails();
//                   VideoSnippet snippet = video.getSnippet();
//
//           // details 없을 시에 저장된 비디오정보 삭제(영상을 몇일 후 공개 되어있는 거)
//           if (details == null || details.getDuration() == null) {
//               return;
//           }
//
//
//           // 조회수
//           boolean viewDisable = true;
//           Long view = null;
//           if (statistics.getViewCount() != null) {
//               viewDisable = false;
//               view = statistics.getViewCount().longValue();
//           }
//
//           // 좋아요수
//           boolean likeDisable = true;
//           Long like = null;
//           if (statistics.getLikeCount() != null) {
//               likeDisable = false;
//               like = statistics.getLikeCount().longValue();
//           }
//
//           // 데이터 수정
//           videoMapper.updateVideoDetailInfo(videoId, view, like);
//
//
//       });
//    }


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
}

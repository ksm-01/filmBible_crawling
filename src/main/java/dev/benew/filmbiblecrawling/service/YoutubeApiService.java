package dev.benew.filmbiblecrawling.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.dto.ChannelDto;
import dev.benew.filmbiblecrawling.dto.PlayListDto;
import dev.benew.filmbiblecrawling.dto.ThumbDto;
import dev.benew.filmbiblecrawling.dto.VideoDto;
import dev.benew.filmbiblecrawling.mapper.*;
import dev.benew.filmbiblecrawling.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    @Value("${youtube.uploadVideo-id}")
    private String uploadVideoId;    // 채널 전체 동영상 카테고리 id

    private final ChannelMapper channelMapper;
    private final PlayListMapper playListMapper;
    private final VideoMapper videoMapper;
    private final PlayListHasVideoMapper phvMapper;
    private final ThumbMapper thumbMapper;
    private final TagMapper tagMapper;

    public YoutubeApiService(ChannelMapper channelMapper, PlayListMapper playListMapper, VideoMapper videoMapper, PlayListHasVideoMapper phvMapper, ThumbMapper thumbMapper, TagMapper tagMapper) {
        this.channelMapper = channelMapper;
        this.playListMapper = playListMapper;
        this.videoMapper = videoMapper;
        this.phvMapper = phvMapper;
        this.thumbMapper = thumbMapper;
        this.tagMapper = tagMapper;
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



    // 필름바이블 재생목록 저장, 업데이트
    public void upsertPlayList() throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        YouTube.Playlists.List request = youtube.playlists()
                .list("id,snippet, status, contentDetails")
                .setKey(youtubeApiKey)
                .setChannelId(channelId)
                .setMaxResults(50L);

        PlaylistListResponse response = request.execute();

        for (Playlist playlist : response.getItems()) {

            PlaylistSnippet snippet = playlist.getSnippet();
            JsonUtil.prettyString(snippet);
            PlaylistContentDetails contentDetails = playlist.getContentDetails();
            System.out.println("-------------------");
            JsonUtil.prettyString(contentDetails);
            PlaylistStatus status = playlist.getStatus();
            System.out.println("----------------------------");
            JsonUtil.prettyString(status);

            String privacyStatus = status.getPrivacyStatus();  // 재생목록 공개 상태
            System.out.println("status : " + privacyStatus);

            String playlistId = playlist.getId();
            String title = snippet.getTitle();
            DateTime dateTime =  snippet.getPublishedAt();

            // 재생목록 생성된 날짜
            LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String thumb = "";

            // 썸네일 있는거로 가져오기
            if (snippet.getThumbnails().getHigh() != null) {
                thumb = snippet.getThumbnails().getHigh().getUrl();
            } else if (snippet.getThumbnails().getMedium() != null) {
                thumb = snippet.getThumbnails().getMedium().getUrl();
            } else {
                thumb = snippet.getThumbnails().getDefault().getUrl();
            }

            String playlistUrl = "https://www.youtube.com/playlist?list=" + playlist.getId();

            // 재생목록에 있는 동영상 개수
            Long videoCount = contentDetails.getItemCount();
            System.out.println("vicdeoCount:" + videoCount);


            PlayListDto dto = PlayListDto.builder()
                    .playListId(playlistId)
                    .playListTitle(title)
                    .playListThumb(thumb)
                    .playListFullPath(playlistUrl)
                    .createdDate(createdDate)
                    .videoCount(videoCount)
                    .playListStatus(privacyStatus)
                    .build();

            // 문자열 깨짐 방지
            String normalized_string = Normalizer.normalize(dto.getPlayListTitle(), Normalizer.Form.NFC);
            dto.setPlayListTitle(normalized_string);

            // 재생목록명, 동영상 개수, 공개 상태 업데이트
            if(playListMapper.findByPlayListId(playlistId) != null) {
                playListMapper.updatePlayList(dto);
            } else {
                playListMapper.savePlayList(dto);
            }
       }
    }


    // 재생목록 비디오 저장, 업데이트
    public void UpsertVideo() throws GeneralSecurityException, IOException {

        YouTube youtube = this.initYoutube();

        List<String> idList = playListMapper.findAllPlayListId();
        List<String> videoIdList = videoMapper.findAllVideoId().stream()
                .map(String :: trim)
                .toList();

        for (String playlistId : idList) {
                YouTube.PlaylistItems.List request = youtube.playlistItems()
                        .list("id,snippet,contentDetails, status")
                        .setKey(youtubeApiKey)
                        .setPlaylistId(playlistId)
                        .setMaxResults(50L);

            String nextPageToken = null;
            do {
                if (nextPageToken != null)
                    request.setPageToken(nextPageToken);

                PlaylistItemListResponse response = request.execute();

                System.out.println("responseSize :" + response.getItems().size());

                for (PlaylistItem item : response.getItems()) {

                    PlaylistItemSnippet snippet = item.getSnippet();
                    JsonUtil.prettyString(snippet);
                    PlaylistItemContentDetails contentDetails = item.getContentDetails();
                    System.out.println("---------------------------");
                    JsonUtil.prettyString(contentDetails);
                    PlaylistItemStatus status = item.getStatus();
                    System.out.println("---------------------------");
                    JsonUtil.prettyString(status);

                    String videoId = contentDetails.getVideoId();
                    String note = contentDetails.getNote();
                    String type = "video";

                    // shorts 판별
                    if (note != null && note.contains("@shorts")) {
                        type = "shorts";
                    }
                    System.out.println("note:" + note);


                    String videoTitle =  snippet.getTitle();

                    String saveThumb = "";

                    if (item.getSnippet().getThumbnails().getHigh() != null) {
                        saveThumb = item.getSnippet().getThumbnails().getHigh().getUrl();
                    } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                        saveThumb = item.getSnippet().getThumbnails().getMedium().getUrl();
                    } else if (item.getSnippet().getThumbnails().getDefault() != null){
                        saveThumb = item.getSnippet().getThumbnails().getDefault().getUrl();
                    }

                    Long videoPosition = snippet.getPosition();
                    System.out.println("videoPosition" + videoPosition);

                    String videoStatus = status.getPrivacyStatus();
                    System.out.println("videoStatus" + videoStatus);
                    String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

                    VideoDto videoDto = VideoDto.builder()
                            .videoId(videoId)
                            .videoTitle(videoTitle)
                            .videoThumb(saveThumb)
                            .videoUrl(videoUrl)
                            .videoType(type)
                            .videoPosition(videoPosition)
                            .videoStatus(videoStatus)
                            .build();

                    System.out.println("Video ID: " + videoId);

                    // 제목 깨짐 방지
                    String normalized_string = Normalizer.normalize(videoDto.getVideoTitle(), Normalizer.Form.NFC);
                    videoDto.setVideoTitle(normalized_string);

                    // 동영상 제목, 재생표시 순서, 공개 상태 업데이트
                    if (videoMapper.findByVideoId(videoId) != null) {
                        videoMapper.updateVideo(videoDto);
                    } else {
                        videoMapper.saveVideo(videoDto);
                    }
                    
                    // 재생목록, 비디오 연결
                    if (phvMapper.countPlayListHasVideo(playlistId, videoId) == 0) {
                        phvMapper.savePlayListHasVideo(playlistId, videoId);
                    }
                }
                nextPageToken = response.getNextPageToken();

            } while (nextPageToken != null);
        }
    }


    // 동영상 정보 갱신 (좋아요, 조회수 등)
   public void updateVideoInfo() throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        List<VideoDto> videoList = videoMapper.findAllVideo();
        System.out.println("videoList : " + videoList );

       for (int i=0; i<videoList.size(); i += 50) {

           List<VideoDto> chunkList = videoList.subList(i, Math.min(i + 50, videoList.size()));

           List<String> videoIds = chunkList.stream()
                   .map(VideoDto::getVideoId)
                   .toList();

           YouTube.Videos.List request = youtube.videos()
                   .list("snippet,contentDetails,statistics")
                   .setId(String.join(",", videoIds))
                   .setKey(youtubeApiKey)
                   .setFields("items(id,snippet(publishedAt),contentDetails(duration),statistics(viewCount,likeCount))");


           VideoListResponse response = request.execute();

           Map<String, Video> videoMapById = response.getItems().stream().collect(Collectors.toMap(
                   Video::getId,
                   it -> it
           ));

           videoMapById.forEach((videoId, video) -> {
               VideoStatistics statistics = video.getStatistics();
               VideoContentDetails details = video.getContentDetails();
               VideoSnippet snippet = video.getSnippet();

               // details 없을 시에 저장된 비디오정보 삭제(영상을 몇일 후 공개 되어있는 거)
               if (details == null || details.getDuration() == null) {
                   return;
               }


               // 조회수
               boolean viewDisable = true;
               Long view = null;
               if (statistics.getViewCount() != null) {
                   viewDisable = false;
                   view = statistics.getViewCount().longValue();
               }

               // 좋아요수
               boolean likeDisable = true;
               Long like = null;
               if (statistics.getLikeCount() != null) {
                   likeDisable = false;
                   like = statistics.getLikeCount().longValue();
               }
               // 재생시간
               String durationStr = details.getDuration();

               log.info("str:{}", durationStr);
               Duration duration = Duration.parse(durationStr);
               LocalTime playTime = LocalTime.ofSecondOfDay(duration.getSeconds());

               // 업로드 날짜
               DateTime dateTime = snippet.getPublishedAt();
               LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                       .atZone(ZoneId.systemDefault())
                       .toLocalDateTime();
               log.info("      view:[{}], like:[{}]", view, like);

               // 데이터 수정
               videoMapper.updateVideoDetailInfo(videoId, playTime, createdDate,viewDisable, view, likeDisable ,like);

           });
       }
    }

    // 비디오 썸네일 사이즈타입별 저장
    public void saveThumb() throws GeneralSecurityException, IOException {
        YouTube youtube = this.initYoutube();

        List<VideoDto> videoList = videoMapper.findAllVideo();

        System.out.println("videoList" + videoList);
        for (int i=0; i<videoList.size(); i += 50) {
            List<VideoDto> chunkList = videoList.subList(i, Math.min(i + 50, videoList.size()));
            List<String> videoIds = chunkList.stream()
                    .map(VideoDto::getVideoId)
                    .toList();

            YouTube.Videos.List req = youtube.videos()
                    .list("snippet")
                    .setId(String.join(",", videoIds))
                    .setKey(youtubeApiKey);

            VideoListResponse res = req.execute();

            JsonUtil.prettyString(res);

            Map<String, Video> videoMapById = res.getItems().stream().collect(Collectors.toMap(
                    Video::getId,
                    it -> it
            ));

            videoMapById.forEach((videoId, video) -> {
                VideoSnippet snippet = video.getSnippet();
                ThumbnailDetails thumbnails = snippet.getThumbnails();


//                Map<String, Thumbnail> thumbMap = Map.of(
//                        "default", thumbnails.getDefault(),
//                        "medium", thumbnails.getMedium(),
//                        "high", thumbnails.getHigh(),
//                        "standard", thumbnails.getStandard(),
//                        "maxres", thumbnails.getMaxres()
//                );

                Map<String, Thumbnail> thumbMap = new HashMap<String, Thumbnail>();
                if(thumbnails.getDefault() != null) {
                    thumbMap.put("default", thumbnails.getDefault());
                }
                if (thumbnails.getMedium() != null) {
                    thumbMap.put("medium", thumbnails.getMedium());
                }
                if (thumbnails.getHigh() != null) {
                    thumbMap.put("high", thumbnails.getHigh());
                }
                if (thumbnails.getStandard() != null) {
                    thumbMap.put("standard", thumbnails.getStandard());
                }
                if (thumbnails.getMaxres() != null) {
                    thumbMap.put("maxres", thumbnails.getMaxres());
                }

                System.out.println("-------" + thumbMap);

                List<ThumbDto> thumbDtoList = new ArrayList<>();
                thumbMap.forEach((sizeType, thumb) -> {
                    if (thumb == null || thumb.getUrl() == null)
                        return;
                    ThumbDto thumbDto = new ThumbDto();
                    thumbDto.setVideoId(videoId);
                    thumbDto.setThumbType(sizeType);
                    thumbDto.setThumbUrl(thumb.getUrl());
                    thumbDtoList.add(thumbDto);
                });


                // 썸네일 저장
                if (!thumbDtoList.isEmpty()) {
                    if (thumbMapper.findThumbById(videoId) != null) {
                        thumbMapper.deleteThumb(videoId);
                    }
                        thumbMapper.saveVideoThumb(thumbDtoList);
                }

                List<String> tagList = snippet.getTags();
                System.out.println("tagList:" + tagList);
                if (!CollectionUtils.isEmpty(tagList)) {
                    if (tagMapper.findTagById(videoId) != null) {
                        tagMapper.deleteTag(videoId);
                    }
                    tagMapper.saveTag(videoId, tagList);
                }

             });
        }
    }


    // 채널 저장, 업데이트
    public void channelInfo() throws GeneralSecurityException, IOException {

        YouTube youtube = this.initYoutube();

        YouTube.Channels.List req = youtube.channels()
                .list("id, snippet, statistics")
                .setId("UCijh44bpBtXO52xkLhb9vUQ")
                .setKey(youtubeApiKey);

        ChannelListResponse res = req.execute();
        System.out.println("channelInfo-------");

        JsonUtil.prettyString(res.getItems());

        for (Channel channel : res.getItems()) {
            ChannelSnippet snippet = channel.getSnippet();
            ChannelStatistics statistics = channel.getStatistics();

            String channelId = channel.getId();
            String channelName = snippet.getTitle();
            DateTime dateTime = snippet.getPublishedAt();
            LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();



            Long subCount = statistics.getSubscriberCount().longValue();
//            Long videoCount = statistics.getVideoCount().longValue();

            ChannelDto channelDto = ChannelDto.builder()
                    .channelId(channelId)
                    .channelName(channelName)
                    .channelSubscriber(subCount)
                    .createdDate(createdDate)
                    .build();

            // 채널명, 구독자 업데이트
            if (channelMapper.findChannelById(channelId) != null) {
                channelMapper.updateChannel(channelDto);
            } else {
                channelMapper.saveChannel(channelDto);
            }
        }
    }

    // 재생목록에 없는 숏츠가져오기
    public void saveShorts() throws GeneralSecurityException, IOException {

        YouTube youtube = this.initYoutube();

            YouTube.PlaylistItems.List request = youtube.playlistItems()
                    .list("id,snippet,contentDetails, status")
                    .setKey(youtubeApiKey)
                    .setPlaylistId(uploadVideoId)
                    .setMaxResults(50L);

            String nextPageToken = null;
            do {
                if (nextPageToken != null)
                    request.setPageToken(nextPageToken);

                PlaylistItemListResponse response = request.execute();

                System.out.println("responseSize :" + response.getItems().size());

                for (PlaylistItem item : response.getItems()) {

                    PlaylistItemSnippet snippet = item.getSnippet();
                    JsonUtil.prettyString(snippet);
                    PlaylistItemContentDetails contentDetails = item.getContentDetails();
                    System.out.println("---------------------------");
                    JsonUtil.prettyString(contentDetails);
                    PlaylistItemStatus status = item.getStatus();
                    System.out.println("---------------------------");
                    JsonUtil.prettyString(status);

                    String videoId = contentDetails.getVideoId();
                    String note = contentDetails.getNote();
                    System.out.println("note:" + note);

                    // 숏츠영상 아닌거 넘어가기
                    if (note != null && !note.contains("@shorts"))
                        continue;

                    String videoTitle =  snippet.getTitle();
                    String type = "shorts";
                    String saveThumb = "";

                    if (item.getSnippet().getThumbnails().getHigh() != null) {
                        saveThumb = item.getSnippet().getThumbnails().getHigh().getUrl();
                    } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                        saveThumb = item.getSnippet().getThumbnails().getMedium().getUrl();
                    } else if (item.getSnippet().getThumbnails().getDefault() != null){
                        saveThumb = item.getSnippet().getThumbnails().getDefault().getUrl();
                    }

                    Long videoPosition = snippet.getPosition();
                    System.out.println("videoPosition" + videoPosition);

                    String videoStatus = status.getPrivacyStatus();
                    System.out.println("videoStatus" + videoStatus);
                    String videoUrl = "https://www.youtube.com/shorts/" + videoId;

                    VideoDto videoDto = VideoDto.builder()
                            .videoId(videoId)
                            .videoTitle(videoTitle)
                            .videoThumb(saveThumb)
                            .videoUrl(videoUrl)
                            .videoType(type)
                            .videoPosition(videoPosition)
                            .videoStatus(videoStatus)
                            .build();

                    System.out.println("Video ID: " + videoId);

                    // 제목 깨짐 방지
                    String normalized_string = Normalizer.normalize(videoDto.getVideoTitle(), Normalizer.Form.NFC);
                    videoDto.setVideoTitle(normalized_string);

                    // 동영상 제목, 재생표시 순서, 공개 상태 업데이트
                    if (videoMapper.findByVideoId(videoId) != null) {
                        videoMapper.updateVideo(videoDto);
                    } else {
                        videoMapper.saveVideo(videoDto);
                    }
                }
                nextPageToken = response.getNextPageToken();

            } while (nextPageToken != null);
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
}

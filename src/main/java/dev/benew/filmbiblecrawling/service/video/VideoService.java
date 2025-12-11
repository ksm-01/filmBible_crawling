package dev.benew.filmbiblecrawling.service.video;

import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.VideoType;
import dev.benew.filmbiblecrawling.dto.VideoDto;
import dev.benew.filmbiblecrawling.mapper.PlayListHasVideoMapper;
import dev.benew.filmbiblecrawling.mapper.PlayListMapper;
import dev.benew.filmbiblecrawling.mapper.VideoMapper;
import dev.benew.filmbiblecrawling.service.YoutubeApiService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {

    private final YoutubeApiService youtubeApiService;
    private final PlayListMapper playListMapper;
    private final VideoMapper videoMapper;
    private final PlayListHasVideoMapper phvMapper;
    private final ShortsCheckService shortsCheckService;

    public VideoService(YoutubeApiService youtubeApiService, PlayListMapper playListMapper, VideoMapper videoMapper, PlayListHasVideoMapper phvMapper, ShortsCheckService shortsCheckService) {
        this.youtubeApiService = youtubeApiService;
        this.playListMapper = playListMapper;
        this.videoMapper = videoMapper;
        this.phvMapper = phvMapper;
        this.shortsCheckService = shortsCheckService;
    }


    public void upsertVideo() throws GeneralSecurityException, IOException {

        System.out.println("비디오 저장 시작 -----------------");

        List<String> playListIds = playListMapper.findAllPlayListId();
        Set<String> videoIdSet = new HashSet<>(videoMapper.findAllVideoId());


        for (String playListId : playListIds) {
            List<PlaylistItem> response = youtubeApiService.playListItemApi(playListId);

            for (PlaylistItem item : response) {

                VideoDto videoDto = this.convertVideoDto(item);

                String videoUrl = VideoType.Video.getUrl();
                String videoFullUrl = videoUrl + videoDto.getVideoId();
                String videoType = VideoType.Video.name();

                videoDto.setVideoUrl(videoFullUrl);

                // 동영상 제목, 재생표시 순서, 공개 상태 업데이트
                if (videoIdSet.contains(videoDto.getVideoId())) {
                    videoMapper.updateVideo(videoDto);
                } else {
                    videoDto.setVideoType(videoType);
                    videoMapper.saveVideo(videoDto);
                    videoIdSet.add(videoDto.getVideoId());
                    // 재생목록, 비디오 연결
                    if (phvMapper.countPlayListHasVideo(playListId, videoDto.getVideoId()) == 0) {
                        phvMapper.savePlayListHasVideo(playListId, videoDto.getVideoId());
                    }
                    System.out.println("새로 저장된 비디오 ID : " + videoDto.getVideoId());
                }
            }

        }
    }

    // 숏츠만 가져오기
    public void upsertOnlyShorts() throws GeneralSecurityException, IOException {

        System.out.println("shorts 가져오기 시작 ----------------------------");

//        Set<String> videoIdSet = new HashSet<>(videoMapper.findAllVideoId());
        List<VideoDto> videoList = videoMapper.findAllVideo();
        Map<String, String> videoMap = videoList.stream()
                .collect(Collectors.toMap(
                        VideoDto :: getVideoId,
                        VideoDto :: getVideoType
                ));

        System.out.println("videoMap :" + videoMap);

        List<PlaylistItem> response = youtubeApiService.playListItemApi(null);

        for (PlaylistItem item : response) {

            VideoDto videoDto = this.convertVideoDto(item);

            String videoUrl = VideoType.Shorts.getUrl();
            String videoFullUrl = videoUrl + videoDto.getVideoId();
            String videoType = VideoType.Shorts.name();

            if (!shortsCheckService.shortsUrlCheck(videoFullUrl)) {
                continue;
            }

            videoDto.setVideoUrl(videoFullUrl);
            videoDto.setVideoType(videoType);

            // 동영상 제목, 재생표시 순서, 공개 상태 업데이트
            if (videoMap.containsKey(videoDto.getVideoId())) {
                if (VideoType.Video.name().equals(videoMap.get(videoDto.getVideoId()))) {
                    videoMapper.updateVideo(videoDto);
                    System.out.println("map value: " + videoMap.get(videoDto.getVideoId()));
                    System.out.println("shorts로 업데이트한 ID : " + videoDto.getVideoId());
                }
            } else {
                videoMapper.saveVideo(videoDto);
                videoMap.put(videoDto.getVideoId(), videoDto.getVideoType());
                System.out.println("새로 저장된 shorts ID : " + videoDto.getVideoId());
            }

//            if (videoIdSet.contains(videoDto.getVideoId())) {
//                videoMapper.updateVideo(videoDto);
//                System.out.println("shorts로 업데이트한 ID : " + videoDto.getVideoId());
//            } else {
//                videoMapper.saveVideo(videoDto);
//                videoIdSet.add(videoDto.getVideoId());
//                System.out.println("새로 저장된 shorts ID : " + videoDto.getVideoId());
//            }
        }
    }


    protected VideoDto convertVideoDto(PlaylistItem item) {
        PlaylistItemSnippet snippet = item.getSnippet();
        PlaylistItemContentDetails contentDetails = item.getContentDetails();
        PlaylistItemStatus status = item.getStatus();

        // 제목 깨짐 방지
        String videoTitle = Normalizer.normalize(snippet.getTitle(), Normalizer.Form.NFC);

//        String videoDescription = snippet.getDescription();

        String videoThumb = null;

        if (snippet.getThumbnails().getHigh() != null) {
            videoThumb = snippet.getThumbnails().getHigh().getUrl();
        } else if (snippet.getThumbnails().getMedium() != null) {
            videoThumb = snippet.getThumbnails().getMedium().getUrl();
        } else if (snippet.getThumbnails().getDefault() != null) {
            videoThumb = snippet.getThumbnails().getDefault().getUrl();
        }

        Long videoPosition = snippet.getPosition();  // 재생목록에 표시되는 순서
        String videoId = contentDetails.getVideoId();
//        String note = contentDetails.getNote();  // 사용자 생성 메모
//        DateTime publishedAt = contentDetails.getVideoPublishedAt();  // 동영상이 youtube에 게시된 날짜
        String show = status.getPrivacyStatus(); // 재생목록 항목의 공개 범위 상태

        return VideoDto.builder()
                .videoId(videoId)
                .videoTitle(videoTitle)
                .videoThumb(videoThumb)
                .videoPosition(videoPosition)
                .videoStatus(show)
                .build();
    }


}

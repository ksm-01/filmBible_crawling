package dev.benew.filmbiblecrawling.service.video;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.dto.VideoDto;
import dev.benew.filmbiblecrawling.mapper.VideoMapper;
import dev.benew.filmbiblecrawling.service.YoutubeApiService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateVideoInfoService {
    private final YoutubeApiService youtubeApiService;
    private final VideoMapper videoMapper;

    public UpdateVideoInfoService(YoutubeApiService youtubeApiService, VideoMapper videoMapper) {
        this.youtubeApiService = youtubeApiService;
        this.videoMapper = videoMapper;
    }

    public void updateVideoInfo() throws GeneralSecurityException, IOException {

        System.out.println("비디오 정보 업데이트 시작 ------------------------------");

        List<VideoDto> videoList = videoMapper.findAllVideo();

        // 50개씩 처리 (youtube api가 50개씩 가능함)
        for (int i = 0; i < videoList.size(); i += 50) {
            List<VideoDto> chunkList = videoList.subList(i, Math.min(i + 50, videoList.size()));

            List<String> videoIds = chunkList.stream()
                    .map(VideoDto::getVideoId)
                    .toList();

            VideoListResponse response = youtubeApiService.videoApi(videoIds);

            Map<String, Video> videoMapById = response.getItems().stream().collect(Collectors.toMap(
                    Video::getId,
                    it -> it
            ));

            videoMapById.forEach((videoId, video) -> {
                VideoDto videoDto = convertVideoDto(video);
                if (videoDto == null)
                    return;
                // 데이터 수정
                videoMapper.updateVideoDetailInfo(videoDto);
            });
        }
    }

    protected VideoDto convertVideoDto(Video video) {

        VideoStatistics statistics = video.getStatistics();
        VideoContentDetails details = video.getContentDetails();
        VideoSnippet snippet = video.getSnippet();

        // details 없으면 비공개 영상일수도
        if (details == null || details.getDuration() == null) {
            return null;
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

        Duration duration = Duration.parse(durationStr);
        LocalTime playTime = LocalTime.ofSecondOfDay(duration.getSeconds());

        // 업로드 날짜
        DateTime dateTime = snippet.getPublishedAt();
        LocalDateTime createdDate = Instant.ofEpochMilli(dateTime.getValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return VideoDto.builder()
                .videoId(video.getId())
                .playTime(playTime)
                .videoCreateDate(createdDate)
                .viewDisable(viewDisable)
                .videoView(view)
                .likeDisable(likeDisable)
                .videoLike(like)
                .build();
    }

}

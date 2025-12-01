package dev.benew.filmbiblecrawling.service;

import com.google.api.services.youtube.model.*;
import dev.benew.filmbiblecrawling.dto.ThumbDto;
import dev.benew.filmbiblecrawling.dto.VideoDto;
import dev.benew.filmbiblecrawling.mapper.TagMapper;
import dev.benew.filmbiblecrawling.mapper.ThumbMapper;
import dev.benew.filmbiblecrawling.mapper.VideoMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThumbnailAndTagService {
    private final YoutubeApiService youtubeApiService;
    private final VideoMapper videoMapper;
    private final ThumbMapper thumbMapper;
    private final TagMapper tagMapper;

    public ThumbnailAndTagService(YoutubeApiService youtubeApiService, VideoMapper videoMapper, ThumbMapper thumbMapper, TagMapper tagMapper) {
        this.youtubeApiService = youtubeApiService;
        this.videoMapper = videoMapper;
        this.thumbMapper = thumbMapper;
        this.tagMapper = tagMapper;
    }

    public void thumbAndTagMapping() throws GeneralSecurityException, IOException {

        System.out.println("썸네일, 태그 저장 시작 -------------------------");

        List<VideoDto> videoList = videoMapper.findAllVideo();
        Set<String> thumbInVideoIdSet = new HashSet<>(thumbMapper.findAllVideoIdsByThumb());
        Set<String> tagInVideoIdSet = new HashSet<>(tagMapper.findAllVideoIdsByTag());


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
                List<ThumbDto> thumbList =  this.thumbList(videoId, video);
                List<String> tagList = this.tagList(video);

                if (!thumbList.isEmpty()) {
                    // 이미 있으면 새로저장
                    if (thumbInVideoIdSet.contains(videoId)) {
                        thumbMapper.deleteThumb(videoId);
                    }
                    thumbMapper.saveVideoThumb(thumbList);
                    thumbInVideoIdSet.add(videoId);
                }

                if (!CollectionUtils.isEmpty(tagList)) {
                    // 이미 있으면 새로저장
                    if (tagInVideoIdSet.contains(videoId)) {
                        tagMapper.deleteTag(videoId);
                    }
                    tagMapper.saveTag(videoId, tagList);
                    tagInVideoIdSet.add(videoId);
                }

            });
        }
    }

    protected List<ThumbDto> thumbList(String videoId, Video video) {
        VideoSnippet snippet = video.getSnippet();
        ThumbnailDetails thumbnails = snippet.getThumbnails();

        Map<String, Thumbnail> thumbMap = new HashMap<>();
        if (thumbnails.getDefault() != null) {
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
        return thumbDtoList;

    }

    protected List<String> tagList(Video video) {
        VideoSnippet snippet = video.getSnippet();
        List<String> tagList = snippet.getTags();
        return tagList;
    }
}

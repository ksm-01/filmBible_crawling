package dev.benew.filmbiblecrawling;

import lombok.Getter;

@Getter
public enum VideoType {
    Video("https://www.youtube.com/watch?v="),
    Shorts("https://www.youtube.com/shorts/");


    private final String url;

    VideoType(String url) {
        this.url = url;
    }
}

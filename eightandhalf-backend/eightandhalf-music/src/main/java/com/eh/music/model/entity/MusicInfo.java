package com.eh.music.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicInfo {
    private Long musicId;
    private String musicUrl;
    private String coverUrl;
    private Long userId;
    private String author;
    private String musicName;
    private String description;
    private Integer subscribeCount;
    private Integer commentCount;
    private Integer visitCount;
    private String musicCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String album;
    private Integer listenCount;
}

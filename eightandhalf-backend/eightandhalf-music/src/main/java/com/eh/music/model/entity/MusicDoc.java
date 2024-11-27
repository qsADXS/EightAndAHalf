package com.eh.music.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicDoc implements Serializable {
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
    private List<? extends Serializable> suggestion;

    public MusicDoc(Music music) {
        this.musicId = music.getMusicId();
        this.musicUrl = music.getMusicUrl();
        this.coverUrl = music.getCoverUrl();
        this.userId = music.getUserId();
        this.author = music.getAuthor();
        this.musicName = music.getMusicName();
        this.description = music.getDescription();
        this.subscribeCount = music.getSubscribeCount();
        this.commentCount = music.getCommentCount();
        this.visitCount = music.getVisitCount();
        this.musicCategory = music.getMusicCategory();
        this.createdAt = music.getCreatedAt();
        this.updatedAt = music.getUpdatedAt();
        this.deletedAt = music.getDeletedAt();
        this.album = music.getAlbum();
        this.suggestion = Arrays.asList(this.author,this.musicName);
    }


}

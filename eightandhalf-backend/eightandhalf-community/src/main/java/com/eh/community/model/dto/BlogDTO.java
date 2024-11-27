package com.eh.community.model.dto;

import com.eh.community.model.entity.Blog;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDTO {
    private Long blogId;

    private Long userId;

    private int likeCount;

    private String blogContent;

    private String url;

    private Long musicId;

    private int commentCount;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime deletedAt;

    private String title;

    public void ConvertToBlogDTO(Blog blog) {
        this.blogId = blog.getBlogId();
        this.userId = blog.getUserId();
        this.likeCount = blog.getLikeCount();
        this.blogContent = blog.getBlogContent();
        this.url = blog.getUrl();
        this.musicId = blog.getMusicId();
        this.commentCount = blog.getCommentCount();
        this.createdAt = blog.getCreatedAt();
        this.updatedAt = blog.getUpdatedAt();
        this.deletedAt = blog.getDeletedAt();
        this.title = blog.getTitle();
    }
}

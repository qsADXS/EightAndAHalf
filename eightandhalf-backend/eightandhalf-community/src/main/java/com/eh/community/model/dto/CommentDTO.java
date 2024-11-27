package com.eh.community.model.dto;

import com.eh.community.model.entity.Comment;
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
public class CommentDTO {

    private Long commentId;

    private Long userId;

    private Long musicId;

    private Long parentId;

    private int likeCount;

    private int childCount;

    private String commentContent;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime deletedAt;

    private Long blogId;

    public void ConvertToCommentDTO(Comment comment) {
        commentId = comment.getCommentId();
        userId = comment.getUserId();
        musicId = comment.getMusicId();
        parentId = comment.getParentId();
        likeCount = comment.getLikeCount();
        childCount = comment.getChildCount();
        commentContent = comment.getCommentContent();
        createdAt = comment.getCreatedAt();
        updatedAt = comment.getUpdatedAt();
        deletedAt = comment.getDeletedAt();
        blogId = comment.getBlogId();
    }
}

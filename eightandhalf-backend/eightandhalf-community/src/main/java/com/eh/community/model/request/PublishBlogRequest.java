package com.eh.community.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishBlogRequest {
    private String title;

    private String content;

    private MultipartFile file;

    private String music_id;
}

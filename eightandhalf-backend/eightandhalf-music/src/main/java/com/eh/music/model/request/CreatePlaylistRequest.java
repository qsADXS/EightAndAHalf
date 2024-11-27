package com.eh.music.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlaylistRequest {
    private String playlistName;
    private MultipartFile playlistCoverFile;
    private String description;
    private Integer isPublic;
}

package com.eh.music.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadMusicRequest {

    MultipartFile musicFile;
    MultipartFile coverFile;
    String musicName;
    String singer;
    String description;
    String musicCategory;
    String album;

}

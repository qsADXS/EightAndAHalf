package com.eh.music.model.dto;

import com.eh.music.model.entity.UserPlaylist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAlbumDTO {
    private List<UserPlaylist> userAlbumList;
    private int total;
}

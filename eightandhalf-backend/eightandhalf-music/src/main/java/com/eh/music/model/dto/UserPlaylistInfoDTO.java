package com.eh.music.model.dto;


import com.eh.music.model.entity.MusicInfo;
import com.eh.music.model.entity.UserPlaylist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPlaylistInfoDTO {
    private UserPlaylist playlistInfo;
    private List<MusicInfo> musicList;
    private int total;
}

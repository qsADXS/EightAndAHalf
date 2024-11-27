package com.eh.music.service;

import com.eh.common.vo.PageBean;
import com.eh.music.model.dto.*;
import com.eh.music.model.entity.Music;
import com.eh.music.model.entity.MusicDoc;
import com.eh.music.model.entity.MusicInfo;
import com.eh.music.model.request.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MusicService {
    void saveUploadMusic(Long uploadUserId, UploadMusicRequest uploadMusicRequest);

    List<MusicInfo> queryUploadMusicForPage(Map<String, Object> map);
    int queryCountOfUploadMusic(Long userId);

    MusicCategoryDTO queryAllMusicCategory();
    UserPlaylistDTO queryUserPlaylistList(Map<String, Object> map);
    UserAlbumDTO queryUserAlbumList(Map<String, Object> map);
    UserPlaylistInfoDTO queryPlaylistInfo(Long playlistId);
    void insertMusic(Long loginUserId, SubscribeMusicRequest subscribeMusicRequest);

    int createPlaylist(Long loginUserId, CreatePlaylistRequest createPlaylistRequest);

    int queryMusicExistPlaylist(SubscribeMusicRequest subscribeMusicRequest);

    int queryExistUserPlaylist(Long loginUserId, CollectPlaylistRequest collectPlaylistRequest);

    void subscribePlaylist(Long loginUserId, CollectPlaylistRequest collectPlaylistRequest);

    void cancelSubscribePlaylist(Long loginUserId, CollectPlaylistRequest collectPlaylistRequest);

    void deletePlaylistByPlaylistId(Long loginUserId, Long playlistId);

    void removeMusicFromPlaylist(Long loginUserId, SubscribeMusicRequest subscribeMusicRequest);

    void setUserPlaylistPublicType(Long loginUserId, int playlistPublic);

    PopularRankDTO queryPopularMusicList(Integer num);

    void subscribeSong(Long loginUserId, Long musicId);

    void setRecentListen(Long loginUserId, Long musicId);

    UserPlaylistInfoDTO queryRecentPlaylistInfo();

    UserPlaylistInfoDTO queryRecommendListByIdList(Long loginUserId);


    PageBean<MusicDoc> search(RequestParams requestParams);

    List<String> getSuggestions(String prefix);

    int updateCommentCount(Long musicId);

    int decrCommentCount(Long musicId);

    int updateVisitCount(Long musicId);

    UserPlaylistInfoDTO querySubscribePlaylistInfo();

    UserPlaylistDTO queryHomePagePlaylist(Integer limit);

    MusicInfo getMusicInfoById(Long musicId);

    List<Music> bulkMusic() throws IOException;

    Long queryMySubscribePlaylistId(Long loginUserId);
}

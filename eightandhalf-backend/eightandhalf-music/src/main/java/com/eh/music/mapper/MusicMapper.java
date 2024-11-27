package com.eh.music.mapper;

import com.eh.music.model.entity.*;
import com.eh.music.model.request.SubscribeMusicRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface MusicMapper {
    int insertUploadMusic(Music music);
    List<MusicInfo> selectUploadMusicForPage(Map<String, Object> map);
    int selectCountOfUploadMusic(Long userId);
    List<MusicCategory> selectAllMusicCategory();

    List<UserPlaylist> selectPlaylistByUserId(Map<String, Object> map);

    List<MusicInfo> selectPlaylistInfoByPlaylistId(Long playlistId);

    int insertMusicIntoPlaylist(SubscribeMusicRequest subscribeMusicRequest);

    int insertPlaylist(Playlist playlist);

    int insertUserPlaylistRelation(Map<String, Object> map);

    int selectMusicExistPlaylist(SubscribeMusicRequest subscribeMusicRequest);

    int selectExistUserPlaylist(Map<String, Object> map);

    int deleteUserPlaylistRelation(Map<String, Object> map);

    List<UserPlaylist> selectAlbumByUserId(Map<String, Object> map);

    int deletePlaylistMusicByPlaylistId(Long playlistId);

    int deleteUserPlaylistByPlaylistId(Long playlistId);

    int deletePlaylistByPlaylistId(Long playlistId);

    int deleteMusicFromPlaylist(SubscribeMusicRequest subscribeMusicRequest);

    int setUserPlaylistPublicType(Map<String, Object> map);

    int addPlaylistMusicSubscribeCount(Long playlistId);

    int reducePlaylistMusicSubscribeCount(Long playlistId);

    int addMusicSubscribeCountByMusicId(Long musicId);

    int reduceMusicSubscribeCountByMusicId(Long musicId);

    List<MusicInfo> selectPopularMusicInfo(Integer num);

    Long selectSpecialPlaylistByName(Map<String, Object> map);
    int updateRecentListenTime(Map<String, Object> map);

    List<MusicInfo> selectMusicInfoByMusicIdList(@Param("ids") List<Long> musicIdLongList);

    List<MusicInfo> selectRecentPlaylistInfoPlaylistId(Long PlaylistId);

    int updateCommentCountByMusicId(Long musicId);

    int decrCommentCountByMusicId(Long musicId);

    int updateVisitCountByMusicId(Long musicId);

    List<UserPlaylist> selectHomePagePlaylist(Integer limit);

    List<MusicInfo> selectSubscribePlaylistInfoPlaylistId(Long playlistId);

    MusicInfo getMusicInfoById(Long musicId);

    UserPlaylist selectPlaylistByPlaylistId(Long playlistId);

    List<Music> getAllmusic();
}

package com.eh.music.constant;

public class Constants {
    public static final int PLAYLIST_CATEGORY_PLAYLIST = 0; //普通歌单
    public static final int PLAYLIST_CATEGORY_ALBUM = 1; //专辑歌单
    public static final int RELATIONSHIP_TYPE_CREATE = 0; //创建
    public static final int RELATIONSHIP_TYPE_SUBSCRIBE = 1; //收藏
    public static final int PLAYLIST_PUBLIC = 1;
    public static final int PLAYLIST_PRIVATE = 0;
    public static final String CACHE_MUSIC_CATEGORY = "musicCategory";
    public static final String CACHE_PLAYLIST_LIST_PREFIX = "playlistList:userId:";
    public static final String CACHE_ALBUM_LIST_PREFIX = "albumList:userId:";
    public static final String CACHE_PLAYLIST_INFO_PREFIX = "playlistInfo:playlistId:";
    public static final String CACHE_POPULAR_MUSIC = "popularMusicList:";
    public static final String USER_SUBSCRIBE_PLAYLIST = "我的收藏";
    public static final String USER_RECENT_LISTEN_PLAYLIST = "最近播放";
    public static final String CACHE_CREATE_RELATION_SUFFIX = ":0";
    public static final String CACHE_SUBSCRIBE_RELATION_SUFFIX = ":1";
    public static final String ELASTIC_SEARCH_IP ="http://106.54.22.249:9200";
    public static final String MUSIC_INDEX = "music_index";
    public static final String CACHE_HOME_PAGE_PLAYLIST = "homePagePlaylist:";
}

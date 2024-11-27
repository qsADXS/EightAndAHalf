package com.eh.music.service.impl;
import com.alibaba.fastjson.JSON;
import com.eh.common.vo.PageBean;
import com.eh.music.model.request.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.eh.common.util.SnowFlakeUtil;
import com.eh.common.util.TencentYunCOSUtil;
import com.eh.common.util.ThreadLocalUtil;
import com.eh.music.constant.Constants;
import com.eh.music.grpc.client.RecommendClient;
import com.eh.music.mapper.MusicMapper;
import com.eh.music.model.dto.*;
import com.eh.music.model.entity.*;
import com.eh.music.service.MusicService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MusicServiceImpl implements MusicService {
    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private RecommendClient recommendClient;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final int EXPIRATION_TIME = 5;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Transactional
    @Override
    public void saveUploadMusic(Long uploadUserId, UploadMusicRequest uploadMusicRequest) {
        Music music = new Music();
        music.setMusicId(SnowFlakeUtil.getSnowFlakeId());
        music.setMusicUrl(TencentYunCOSUtil.upload(uploadMusicRequest.getMusicFile()));
        music.setCoverUrl(TencentYunCOSUtil.upload(uploadMusicRequest.getCoverFile()));
        music.setUserId(uploadUserId);
        music.setMusicName(uploadMusicRequest.getMusicName());
        music.setAuthor(uploadMusicRequest.getSinger());
        music.setDescription(uploadMusicRequest.getDescription());
        music.setMusicCategory(uploadMusicRequest.getMusicCategory());
        music.setVisitCount(0);
        music.setCommentCount(0);
        music.setSubscribeCount(0);
        music.setAlbum(uploadMusicRequest.getAlbum());
        music.setCreatedAt(LocalDateTime.now());
        music.setUpdatedAt(LocalDateTime.now());
        music.setDeletedAt(LocalDateTime.now());
        musicMapper.insertUploadMusic(music);
        MusicDoc musicDoc = new MusicDoc(music);

        //异步更新es
        rabbitTemplate.convertAndSend("music.topic","music.update",musicDoc);
        //异步传输grpc
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("musicId", String.valueOf(music.getMusicId()));
            map.put("musicName", uploadMusicRequest.getMusicFile().getOriginalFilename());
            map.put("musicFile", uploadMusicRequest.getMusicFile().getBytes());
            rabbitTemplate.convertAndSend("music.topic", "music.grpc", map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MusicInfo> queryUploadMusicForPage(Map<String, Object> map) {
        return musicMapper.selectUploadMusicForPage(map);
    }

    @Override
    public int queryCountOfUploadMusic(Long userId) {
        return musicMapper.selectCountOfUploadMusic(userId);
    }

    @Override
    public MusicCategoryDTO queryAllMusicCategory() { //添加redis缓存 key:"musicCategory"
        String cacheKey = Constants.CACHE_MUSIC_CATEGORY;
        MusicCategoryDTO musicCategoryDTO = (MusicCategoryDTO) redisTemplate.opsForValue().get(cacheKey);
        if (musicCategoryDTO == null) {
            musicCategoryDTO = new MusicCategoryDTO();
            List<MusicCategory> musicCategoryList = musicMapper.selectAllMusicCategory();
            musicCategoryDTO.setMusicCategoryList(musicCategoryList);
            musicCategoryDTO.setTotal(musicCategoryList.size());
            redisTemplate.opsForValue().set(cacheKey, musicCategoryDTO, EXPIRATION_TIME, TimeUnit.MINUTES);
        }
        return musicCategoryDTO;
    }
    @Override
    public UserPlaylistDTO queryUserPlaylistList(Map<String, Object> map) { //key:"playlistList:" + userId
        Long userId = (Long) map.get("userId");
        String cacheKey = Constants.CACHE_PLAYLIST_LIST_PREFIX + userId + ":"+ map.get("relationshipType");
        String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
        UserPlaylistDTO userPlaylistDTO;
        if (cachedData == null || StrUtil.isBlankIfStr(cachedData)) {
            //查询数据库
            userPlaylistDTO = new UserPlaylistDTO();
            List<UserPlaylist> userPlaylistList = musicMapper.selectPlaylistByUserId(map);
            userPlaylistDTO.setUserPlaylistList(userPlaylistList);
            userPlaylistDTO.setTotal(userPlaylistList.size());
            //添加缓存
            JSONObject jsonObject = new JSONObject(userPlaylistDTO);
            redisTemplate.opsForValue().set(cacheKey, jsonObject.toString(), EXPIRATION_TIME, TimeUnit.MINUTES);
        } else {
            //查询redis
            userPlaylistDTO = JSONUtil.toBean(cachedData, UserPlaylistDTO.class);
        }
        return userPlaylistDTO;
    }
    @Override
    public UserAlbumDTO queryUserAlbumList(Map<String, Object> map) {
        Long userId = (Long) map.get("userId");
        String cacheKey = Constants.CACHE_ALBUM_LIST_PREFIX + userId + ":"+ map.get("relationshipType");
        String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
        UserAlbumDTO userAlbumDTO;
        if (cachedData == null || StrUtil.isBlankIfStr(cachedData)) {
            //查询数据库
            userAlbumDTO = new UserAlbumDTO();
            List<UserPlaylist> userAlbumList = musicMapper.selectAlbumByUserId(map);
            userAlbumDTO.setUserAlbumList(userAlbumList);
            userAlbumDTO.setTotal(userAlbumList.size());
            //添加缓存
            JSONObject jsonObject = new JSONObject(userAlbumDTO);
            redisTemplate.opsForValue().set(cacheKey, jsonObject.toString(), EXPIRATION_TIME, TimeUnit.MINUTES);
        } else {
            //查询redis
            userAlbumDTO = JSONUtil.toBean(cachedData, UserAlbumDTO.class);
        }
        return userAlbumDTO;
    }
    @Override
    public UserPlaylistInfoDTO queryPlaylistInfo(Long playlistId) {
        String cacheKey = Constants.CACHE_PLAYLIST_INFO_PREFIX + playlistId;
        String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
        UserPlaylistInfoDTO userPlaylistInfoDTO;
        if (cachedData == null || StrUtil.isBlankIfStr(cachedData)) {
            //查询数据库
            userPlaylistInfoDTO = new UserPlaylistInfoDTO();
            UserPlaylist playlistInfo = musicMapper.selectPlaylistByPlaylistId(playlistId);
            List<MusicInfo> musicInfoList = musicMapper.selectPlaylistInfoByPlaylistId(playlistId);
            userPlaylistInfoDTO.setPlaylistInfo(playlistInfo);
            userPlaylistInfoDTO.setMusicList(musicInfoList);
            userPlaylistInfoDTO.setTotal(musicInfoList.size());
            //最近播放不用建立缓存
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("userId", ThreadLocalUtil.get());
            rMap.put("playlistName", Constants.USER_RECENT_LISTEN_PLAYLIST);
            Long rPlaylistId = musicMapper.selectSpecialPlaylistByName(rMap);
            if (!playlistId.equals(rPlaylistId)) {
                //添加缓存
                JSONObject jsonObject = new JSONObject(userPlaylistInfoDTO);
                redisTemplate.opsForValue().set(cacheKey, jsonObject.toString(), EXPIRATION_TIME, TimeUnit.MINUTES);
            }
        } else {
            //查询redis
            userPlaylistInfoDTO = JSONUtil.toBean(cachedData, UserPlaylistInfoDTO.class);
        }
        return userPlaylistInfoDTO;
    }
    @Transactional
    @Override
    public int createPlaylist(Long loginUserId, CreatePlaylistRequest createPlaylistRequest) {
        Playlist playlist = new Playlist();
        Long playlistId = SnowFlakeUtil.getSnowFlakeId();
        playlist.setPlaylistId(playlistId);
        playlist.setPlaylistName(createPlaylistRequest.getPlaylistName());
        playlist.setPlaylistCoverUrl(TencentYunCOSUtil.upload(createPlaylistRequest.getPlaylistCoverFile()));
        playlist.setPlaylistType(Constants.PLAYLIST_CATEGORY_PLAYLIST);
        playlist.setDescription(createPlaylistRequest.getDescription());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        playlist.setDeletedAt(LocalDateTime.now());
        int count = 0;
        count += musicMapper.insertPlaylist(playlist);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistId", playlistId);
        map.put("relationshipType", Constants.RELATIONSHIP_TYPE_CREATE);
        map.put("isPublic", createPlaylistRequest.getIsPublic());
        count += musicMapper.insertUserPlaylistRelation(map);
        return count;
    }

    @Override
    public int queryMusicExistPlaylist(SubscribeMusicRequest subscribeMusicRequest) {
        return musicMapper.selectMusicExistPlaylist(subscribeMusicRequest);
    }

    @Override
    public int queryExistUserPlaylist(Long loginUserId, CollectPlaylistRequest collectPlaylistRequest) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistId", collectPlaylistRequest.getPlaylistId());
        return musicMapper.selectExistUserPlaylist(map);
    }
    @Transactional
    @Override
    public void subscribePlaylist(Long loginUserId, CollectPlaylistRequest collectPlaylistRequest) { //删除缓存 key:"playlistList:" + userId 或 "albumList:" + userId
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistId", collectPlaylistRequest.getPlaylistId());
        map.put("relationshipType", Constants.RELATIONSHIP_TYPE_SUBSCRIBE);
        map.put("isPublic", Constants.PLAYLIST_PUBLIC);
        musicMapper.insertUserPlaylistRelation(map);
        //subscribe++
        musicMapper.addPlaylistMusicSubscribeCount(collectPlaylistRequest.getPlaylistId());
        //"playlistList:1:" + userId
        String cacheKeyOne = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyTwo = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        try {
            redisTemplate.delete(cacheKeyOne);
            redisTemplate.delete(cacheKeyTwo);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyTwo);
        }
    }
    @Transactional
    @Override
    public void cancelSubscribePlaylist(Long loginUserId, CollectPlaylistRequest collectPlaylistRequest) { //删除缓存 "playlistList:" + userId 或 "albumList:" + userId
        Map<String ,Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistId", collectPlaylistRequest.getPlaylistId());
        musicMapper.deleteUserPlaylistRelation(map);
        //subscribe--
        musicMapper.reducePlaylistMusicSubscribeCount(collectPlaylistRequest.getPlaylistId());
        //删除缓存
        String cacheKeyOne = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyTwo = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        try {
            redisTemplate.delete(cacheKeyOne);
            redisTemplate.delete(cacheKeyTwo);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyTwo);
        }
    }
    @Transactional
    @Override
    public void deletePlaylistByPlaylistId(Long loginUserId, Long playlistId) { //删除缓存 删除缓存 "playlistList:" + userId " playlistInfo:" + playlistId

        rabbitTemplate.convertAndSend("music.topic", "music.deletePlaylist", playlistId);

        //删除缓存 "playlistList:" + userId |"albumList:" + userId | "playlistInfo:" + playlistId
        String cacheKeyOne = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyTwo = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyThree = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFour = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFive = Constants.CACHE_PLAYLIST_INFO_PREFIX + playlistId;
        try {
            redisTemplate.delete(cacheKeyOne);
            redisTemplate.delete(cacheKeyTwo);
            redisTemplate.delete(cacheKeyThree);
            redisTemplate.delete(cacheKeyFour);
            redisTemplate.delete(cacheKeyFive);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyTwo);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyThree);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFour);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFive);
        }
    }
    @Transactional
    @Override
    public void insertMusic(Long loginUserId, SubscribeMusicRequest subscribeMusicRequest) { //删除缓存
        subscribeMusicRequest.setCreatedAt(LocalDateTime.now());
        musicMapper.insertMusicIntoPlaylist(subscribeMusicRequest); //向歌单添加音乐
        //subscribeCount++
        musicMapper.addMusicSubscribeCountByMusicId(subscribeMusicRequest.getMusicId());
        //处理缓存
        String cacheKeyOne = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyTwo = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyThree = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFour = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFive = Constants.CACHE_PLAYLIST_INFO_PREFIX + subscribeMusicRequest.getPlaylistId();
        try {
            redisTemplate.delete(cacheKeyOne);
            redisTemplate.delete(cacheKeyTwo);
            redisTemplate.delete(cacheKeyThree);
            redisTemplate.delete(cacheKeyFour);
            redisTemplate.delete(cacheKeyFive);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyTwo);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyThree);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFour);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFive);
        }
    }
    @Transactional
    @Override
    public void removeMusicFromPlaylist(Long loginUserId, SubscribeMusicRequest subscribeMusicRequest) { //删除缓存
        musicMapper.deleteMusicFromPlaylist(subscribeMusicRequest);//从歌单移除音乐
        //subscribeCount--
        musicMapper.reduceMusicSubscribeCountByMusicId(subscribeMusicRequest.getMusicId());
        //处理缓存
        String cacheKeyOne = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyTwo = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyThree = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFour = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFive = Constants.CACHE_PLAYLIST_INFO_PREFIX + subscribeMusicRequest.getPlaylistId();
        try {
            redisTemplate.delete(cacheKeyOne);
            redisTemplate.delete(cacheKeyTwo);
            redisTemplate.delete(cacheKeyThree);
            redisTemplate.delete(cacheKeyFour);
            redisTemplate.delete(cacheKeyFive);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyTwo);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyThree);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFour);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFive);
        }
    }

    @Override
    public void setUserPlaylistPublicType(Long loginUserId, int playlistPublic) { //删除缓存
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("isPublic", playlistPublic);
        musicMapper.setUserPlaylistPublicType(map);
        //删除缓存
        String cacheKeyOne = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyTwo = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_CREATE_RELATION_SUFFIX;
        String cacheKeyThree = Constants.CACHE_PLAYLIST_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        String cacheKeyFour = Constants.CACHE_ALBUM_LIST_PREFIX + loginUserId + Constants.CACHE_SUBSCRIBE_RELATION_SUFFIX;
        try {
            redisTemplate.delete(cacheKeyOne);
            redisTemplate.delete(cacheKeyTwo);
            redisTemplate.delete(cacheKeyThree);
            redisTemplate.delete(cacheKeyFour);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyTwo);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyThree);
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyFour);
        }
    }

    @Override
    public PopularRankDTO queryPopularMusicList(Integer num) {
        String cacheKey = Constants.CACHE_POPULAR_MUSIC + "num:" + num;
        String cacheData = (String) redisTemplate.opsForValue().get(cacheKey);
        PopularRankDTO popularRankDTO;
        if (cacheData == null || StrUtil.isBlankIfStr(cacheData)) {
            //查询数据库
            popularRankDTO = new PopularRankDTO();
            List<MusicInfo> musicInfoList = musicMapper.selectPopularMusicInfo(num);
            popularRankDTO.setMusicList(musicInfoList);
            popularRankDTO.setTotal(musicInfoList.size());
            //添加缓存
            JSONObject jsonObject = new JSONObject(popularRankDTO);
            redisTemplate.opsForValue().set(cacheKey, jsonObject.toString(), EXPIRATION_TIME, TimeUnit.MINUTES);
            return popularRankDTO;
        } else {
            popularRankDTO = JSONUtil.toBean(cacheData, PopularRankDTO.class);
        }
        return popularRankDTO;
    }
    @Transactional
    @Override
    public void subscribeSong(Long loginUserId, Long musicId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistName", Constants.USER_SUBSCRIBE_PLAYLIST);
        Long playlistId = musicMapper.selectSpecialPlaylistByName(map);
        SubscribeMusicRequest subscribeMusicRequest = new SubscribeMusicRequest();
        subscribeMusicRequest.setMusicId(musicId);
        subscribeMusicRequest.setPlaylistId(playlistId);
        subscribeMusicRequest.setCreatedAt(LocalDateTime.now());
        musicMapper.insertMusicIntoPlaylist(subscribeMusicRequest);
        //subscribeCount++
        musicMapper.addMusicSubscribeCountByMusicId(musicId);
        //处理缓存
        String cacheKeyOne = Constants.CACHE_PLAYLIST_INFO_PREFIX + playlistId;
        try {
            redisTemplate.delete(cacheKeyOne);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("music.topic", "music.delete", cacheKeyOne);
        }
    }
    @Transactional
    @Override
    public void setRecentListen(Long loginUserId, Long musicId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistName", Constants.USER_RECENT_LISTEN_PLAYLIST);
        Long playlistId = musicMapper.selectSpecialPlaylistByName(map);
        System.out.println(playlistId);
        //判断歌曲是否存在
        int count = musicMapper.selectMusicExistPlaylist(new SubscribeMusicRequest(musicId, playlistId, null));
        if (count == 0) {
            SubscribeMusicRequest subscribeMusicRequest = new SubscribeMusicRequest();
            subscribeMusicRequest.setMusicId(musicId);
            subscribeMusicRequest.setPlaylistId(playlistId);
            subscribeMusicRequest.setCreatedAt(LocalDateTime.now());
            musicMapper.insertMusicIntoPlaylist(subscribeMusicRequest);
        } else { //更新时间
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("createdAt", LocalDateTime.now());
            updateMap.put("playlistId", playlistId);
            updateMap.put("musicId", musicId);
            musicMapper.updateRecentListenTime(updateMap);
        }
    }
    @Transactional
    @Override
    public UserPlaylistInfoDTO queryRecentPlaylistInfo() {
        Map<String, Object> rMap = new HashMap<>();
        rMap.put("userId", ThreadLocalUtil.get());
        rMap.put("playlistName", Constants.USER_RECENT_LISTEN_PLAYLIST);
        Long rPlaylistId = musicMapper.selectSpecialPlaylistByName(rMap);
        UserPlaylistInfoDTO userPlaylistInfoDTO=  new UserPlaylistInfoDTO();
        List<MusicInfo> musicInfoList = musicMapper.selectRecentPlaylistInfoPlaylistId(rPlaylistId);
        userPlaylistInfoDTO.setMusicList(musicInfoList);
        userPlaylistInfoDTO.setTotal(musicInfoList.size());
        return userPlaylistInfoDTO;
    }

    @Override
    public UserPlaylistInfoDTO queryRecommendListByIdList(Long loginUserId) {
        List<String> musicIdList = recommendClient.getMusic(String.valueOf(loginUserId));
        if (musicIdList == null) {
            musicIdList = new ArrayList<>();
        }
        List<Long> musicIdLongList = musicIdList.stream()
                .map(Long::parseLong) // 转换为 Long 类型
                .collect(Collectors.toList());
        UserPlaylistInfoDTO userPlaylistInfoDTO = new UserPlaylistInfoDTO();
        List<MusicInfo> musicInfoList = musicMapper.selectMusicInfoByMusicIdList(musicIdLongList);
        userPlaylistInfoDTO.setMusicList(musicInfoList);
        userPlaylistInfoDTO.setTotal(musicInfoList.size());
        return userPlaylistInfoDTO;
    }

    @Override
    public PageBean<MusicDoc> search(RequestParams requestParams) {
        try {

            SearchRequest request = new SearchRequest("music_index");
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            String key = requestParams.getKey();
            if(StrUtil.isEmpty(key)){
                boolQuery.must(QueryBuilders.matchAllQuery());
            }else {
                boolQuery.must(QueryBuilders.matchQuery("all",key));
            }
            if(!StrUtil.isEmpty(requestParams.getSortBy())){
                boolQuery.filter(QueryBuilders.termQuery("musicCategory", requestParams.getSortBy()));
            }

            request.source().query(boolQuery);

            if(requestParams.getPageNum()!=null&&requestParams.getPageSize()!=null){
                int pageNum = requestParams.getPageNum();
                int pageSize = requestParams.getPageSize();
                request.source().from((pageNum - 1) * pageSize).size(pageSize);
            }

            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PageBean<MusicDoc> handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits().value;
        //System.out.println("总条数为:"+total);
        SearchHit[] hits = searchHits.getHits();

        List<MusicDoc> musicDocList = new ArrayList<>();
        for(SearchHit hit : hits){
            String json = hit.getSourceAsString();
            MusicDoc musicDoc = JSONUtil.toBean(json, MusicDoc.class);
            musicDocList.add(musicDoc);
        }
        //System.out.println(response);
        return new PageBean<>(musicDocList, total);
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        try {
            SearchRequest request = new SearchRequest(Constants.MUSIC_INDEX);
            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "suggestions",
                    SuggestBuilders.completionSuggestion("suggestion")
                            .prefix(prefix)
                            .skipDuplicates(true)
                            .size(10)
            ));
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            //解析结果
            Suggest suggest = response.getSuggest();
            List<String> result = new ArrayList<>();
            CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
            List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
            for (CompletionSuggestion.Entry.Option option : options) {
                result.add(option.getText().toString());
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int updateCommentCount(Long musicId) {
        return musicMapper.updateCommentCountByMusicId(musicId);
    }

    @Override
    public int updateVisitCount(Long musicId) {
        return musicMapper.updateVisitCountByMusicId(musicId);
    }

    @Override
    public UserPlaylistInfoDTO querySubscribePlaylistInfo() {
        Map<String, Object> sMap = new HashMap<>();
        sMap.put("userId", ThreadLocalUtil.get());
        sMap.put("playlistName", Constants.USER_SUBSCRIBE_PLAYLIST);
        Long sPlaylistId = musicMapper.selectSpecialPlaylistByName(sMap);
        UserPlaylistInfoDTO userPlaylistInfoDTO = new UserPlaylistInfoDTO();
        UserPlaylist playlistInfo = musicMapper.selectPlaylistByPlaylistId(sPlaylistId);
        List<MusicInfo> musicInfoList = musicMapper.selectSubscribePlaylistInfoPlaylistId(sPlaylistId);
        userPlaylistInfoDTO.setPlaylistInfo(playlistInfo);
        userPlaylistInfoDTO.setMusicList(musicInfoList);
        userPlaylistInfoDTO.setTotal(musicInfoList.size());
        return userPlaylistInfoDTO;
    }

    @Override
    public UserPlaylistDTO queryHomePagePlaylist(Integer limit) { //
        String cacheKey = Constants.CACHE_HOME_PAGE_PLAYLIST;
        String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
        UserPlaylistDTO userPlaylistDTO;
        if (cachedData == null || StrUtil.isBlankIfStr(cachedData)) {
            //查询数据库
            userPlaylistDTO = new UserPlaylistDTO();
            List<UserPlaylist> userPlaylistList = musicMapper.selectHomePagePlaylist(limit);
            userPlaylistDTO.setUserPlaylistList(userPlaylistList);
            userPlaylistDTO.setTotal(userPlaylistList.size());
            //添加缓存
            JSONObject jsonObject = new JSONObject(userPlaylistDTO);
            redisTemplate.opsForValue().set(cacheKey, jsonObject.toString(), EXPIRATION_TIME, TimeUnit.MINUTES);
        } else {
            //查询redis
            userPlaylistDTO = JSONUtil.toBean(cachedData, UserPlaylistDTO.class);
        }
        return userPlaylistDTO;
    }

    @Override
    public MusicInfo getMusicInfoById(Long musicId) {
        return musicMapper.getMusicInfoById(musicId);
    }

    @Override
    public int decrCommentCount(Long musicId) {
        return musicMapper.decrCommentCountByMusicId(musicId);
    }


    @Override
    public List<Music> bulkMusic() throws IOException {
        BulkRequest request = new BulkRequest();

        List<Music> musics = musicMapper.getAllmusic();
        for(Music music:musics){
            MusicDoc musicDoc = new MusicDoc(music);
            request.add(new IndexRequest("music_index")
                    .id(musicDoc.getMusicId().toString())
                    .source(JSON.toJSONString(musicDoc), XContentType.JSON));
        }
        restHighLevelClient.bulk(request,RequestOptions.DEFAULT);

        return null;

    }

    @Override
    public Long queryMySubscribePlaylistId(Long loginUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId);
        map.put("playlistName", Constants.USER_SUBSCRIBE_PLAYLIST);
        return musicMapper.selectSpecialPlaylistByName(map);
    }
}

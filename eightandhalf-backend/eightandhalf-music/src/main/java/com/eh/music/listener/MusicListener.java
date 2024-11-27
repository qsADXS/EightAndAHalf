package com.eh.music.listener;


import com.alibaba.fastjson.JSON;
import com.eh.music.constant.Constants;
import com.eh.music.grpc.client.RecommendClient;
import com.eh.music.mapper.MusicMapper;
import com.eh.music.model.entity.MusicDoc;
import com.eh.music.utils.PersistentMultipartFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusicListener {

    private static final int MAX_RETRIES = 3;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    MusicMapper musicMapper;

    @Autowired
    RecommendClient recommendClient;


    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "music.queue1"),
            exchange = @Exchange(name = "music.topic",type = ExchangeTypes.TOPIC),key = "music.delete"))
    public void listenMusicQueue1(String cacheKey){
        boolean success = deleteCacheWithRetry(cacheKey, 0);
        if (success) {
            log.info("Successfully deleted cache for key: " + cacheKey);
        } else {
            log.error("Failed to delete cache for key: " + cacheKey + " after maximum retries.");
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "music.queue2"),
            exchange = @Exchange(name = "music.topic",type = ExchangeTypes.TOPIC),key = "music.update"))
    public void listenMusicQueue2(MusicDoc musicDoc){
        try {
            IndexRequest request = new IndexRequest(Constants.MUSIC_INDEX).id(musicDoc.getMusicId().toString());
            request.source(JSON.toJSONString(musicDoc), XContentType.JSON);
            log.info("JSON字符串"+JSON.toJSONString(musicDoc));
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.info("Failed to index music to ES");
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "music.queue3"),
            exchange = @Exchange(name = "music.topic", type = ExchangeTypes.TOPIC),
            key = "music.deletePlaylist"))
    public void listenDeletePlaylist(Long playlistId) {
        //异步处理
        //处理subscribeCount--
        musicMapper.reducePlaylistMusicSubscribeCount(playlistId);
        //先删除歌单中的歌
        musicMapper.deletePlaylistMusicByPlaylistId(playlistId);
        //再删除用户与歌单的关系
        musicMapper.deleteUserPlaylistByPlaylistId(playlistId);
        //最后删除歌单
        musicMapper.deletePlaylistByPlaylistId(playlistId);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "music.queue4"),
            exchange = @Exchange(name = "music.topic", type = ExchangeTypes.TOPIC),
            key = "music.grpc"))
    public void uploadMusicByGRPC(Map<String, Object> map) {
        try {
            // 从消息中提取 musicId 和 musicFile
            String musicId = (String) map.get("musicId");
            String musicName = (String) map.get("musicName");
            byte[] musicFileBytes = (byte[]) map.get("musicFile");
            // 将字节数组恢复为 MultipartFile
            MultipartFile musicFile = new MockMultipartFile(
                    "file",                         // 文件字段名
                    musicId + ".mp3",                // 文件名
                    "audio/mp3",                     // 文件类型
                    musicFileBytes                   // 文件内容（字节数组）
            );
            // 进行文件处理，如上传
            recommendClient.uploadMusic(musicId, musicFile);
            // 传输完后删除本地文件
            String uploadPath = System.getProperty("user.dir") + "/uploads";
            Path dirPath = Paths.get(uploadPath);
            PersistentMultipartFileUtil.deleteDirectoryAndFiles(dirPath);
        } catch (Exception e) {
            throw new RuntimeException("gRPC 传输失败，回滚事务", e);
        }
    }

    private Boolean deleteCacheWithRetry(String cacheKey, int attempt) {
        if (attempt >= MAX_RETRIES) {
            return false;
        }
        try {
            Boolean result = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(result)) {
                return true;
            } else {
                log.error("Attempt" + (attempt + 1) + ": Failed to delete cache for key: " + cacheKey);
                return deleteCacheWithRetry(cacheKey, attempt + 1);
            }
        } catch (Exception e) {
            log.error("Attempt" + (attempt + 1) + ": Exception occurred while deleting cache for key: " + cacheKey + ", Error: " + e.getMessage());
            return deleteCacheWithRetry(cacheKey, attempt + 1);
        }
    }

}

package com.eh.community.cache;

import com.eh.community.model.dto.CommentDTO;
import com.eh.community.model.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class  RedisService<T> {



    @Autowired
    private RedisTemplate redisTemplate;

    public boolean IsExists(String key) {
        return redisTemplate.getExpire(key) > 0;
    }

    public String getFollowingKey(String uid) {
        return new StringBuilder().append("user-").append(uid).append(":: followings").toString();
    }

    public String getFollowerKey(String uid) {
        return new StringBuilder().append("user-").append(uid).append(":: followers").toString();
    }

    public String getFriendKey(String uid) {
        return new StringBuilder().append("user-").append(uid).append(":: friends").toString();
    }

    public String getUserInfoKey(String uid) {
        return new StringBuilder().append("user-").append(uid).append(":: userInfo").toString();
    }

    public String getBlogKey(String uid) {
        return new StringBuilder().append("user-").append(uid).append(":: blogs").toString();
    }

    public String getCommentInMusicKey(String music_id) {
        return new StringBuilder().append("music_id-").append(music_id).append(":: comments").toString();
    }

    public String getCommentInBlogKey(String blog_id) {
        return new StringBuilder().append("blog_id-").append(blog_id).append(":: comments").toString();
    }

    public String getCommentInParentCommentKey(String comment_id) {
        return new StringBuilder().append("comment_id-").append(comment_id).append(":: comments").toString();
    }

    public String getLikeInCommentKey(String comment_id) {
        return new StringBuilder().append("comment_id-").append(comment_id).append(":: likes").toString();
    }

    public String getLikeInBlogKey(String blog_id) {
        return new StringBuilder().append("blog_id-").append(blog_id).append(":: likes").toString();
    }

    public String getCommentKey(String comment_id) {
        return new StringBuilder().append("comment_id-").append(comment_id).append(":: data").toString();
    }

    public boolean removeInfo(String key) {
        return redisTemplate.delete(key);
    }

    public void incrData(String key) {
                redisTemplate.boundValueOps(key).increment();
                redisTemplate.expire(key, Duration.ofDays(7));
    }

    public void decrData(String key) {
        if (getLikeCount(key) > 0) {
                    redisTemplate.boundValueOps(key).decrement();
                    redisTemplate.expire(key, Duration.ofDays(7));
        }
    }

    public int getLikeCount(String key) {
        return (int) redisTemplate.boundValueOps(key).get();
    }

    public void addComment(String key, Long commentId) {
        redisTemplate.boundZSetOps(key).add(commentId, LocalDateTime.now().getNano());
        redisTemplate.expire(key, Duration.ofHours(1));
    }

    public void removeData(String key, T data) {
        redisTemplate.boundZSetOps(key).remove(data);
    }

    public void setData(String key, T Data) {
        // 连接池的使用导致需要这样写
        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.boundValueOps(key).set(Data);
                operations.expire(key, Duration.ofDays(1));
                operations.exec();
                return null;
            }
        });
    }

    public void setData(String key, T Data, int minute) {
        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                redisTemplate.multi();

                redisTemplate.boundValueOps(key).set(Data);
                redisTemplate.expire(key, Duration.ofMinutes(minute));

                redisTemplate.exec();
                return null;
            }
        });


    }

    public T getData(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }



}

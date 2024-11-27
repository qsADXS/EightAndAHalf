package com.eh.community.listener;

import com.eh.api.client.MusicClient;
import com.eh.common.config.FileConstant;
import com.eh.community.cache.RedisService;
import com.eh.community.mapper.CommunityMapper;
import com.eh.community.model.entity.Comment;
import com.eh.community.model.entity.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommunityListener {

    private static final int MAX_RETRIES = 3;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MusicClient musicClient;

    @Autowired
    private CommunityMapper communityMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "community.queue"),
            exchange = @Exchange(name = "community.topic", type = ExchangeTypes.TOPIC),
            key = "community.delete"))
    public void listenCommunityQueue(String key) {

        boolean success = deleteCacheWithRetry(key, 0);
        if (success) {
            log.info("Successfully deleted cache for key: " + key);
        } else {
            log.error("Failed to delete cache for key: " + key + " after maximum retries.");
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "community.comment1"),
            exchange = @Exchange(name = "community.comment1", type = ExchangeTypes.TOPIC),
            key = "community.create1"))
    public void listenCommentCreate(Comment comment) {
        log.info("Received create comment msg: " + comment);

//        if (comment.getMusicId() != null) {
//            musicClient.updateCommentCount(comment.getMusicId());
//        }

        if (comment.getBlogId() != null) {
            communityMapper.incrBlogCommentCount(comment.getBlogId());
        }

        if (comment.getParentId() != null) {
            communityMapper.incrParentCommentCount(comment.getParentId());
        }

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "community.comment2"),
            exchange = @Exchange(name = "community.comment2", type = ExchangeTypes.TOPIC),
            key = "community.delete2"))
    public void listenCommentDelete(Comment comment) {
        log.info("receive delete comment message:" + comment);
        communityMapper.deleteAllLikeInComment(comment.getCommentId());
        communityMapper.deleteComment(comment.getCommentId());

        if (comment.getBlogId() != null) {
            communityMapper.decrBlogCommentCount(comment.getBlogId());
        }

//        if (comment.getMusicId() != null) {
//            musicClient.decrCommentCount(comment.getMusicId());
//        }

        if (comment.getParentId() != null) {
            communityMapper.decrParentCommentCount(comment.getParentId());
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "community.like3"),
            exchange = @Exchange(name = "community.like3", type = ExchangeTypes.TOPIC),
            key = "community.create3"))
    public void listenLikeCreate(Like like) {
        log.info("receive create like message:  " + like);
        if (like.getCommentId() != null) {
            communityMapper.incrCommentLikeCount(like.getCommentId());
        } else if (like.getBlogId() != null) {
            communityMapper.incrBlogLikeCount(like.getBlogId());
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "community.like4"),
            exchange = @Exchange(name = "community.like4", type = ExchangeTypes.TOPIC),
            key = "community.delete4"))
    public void listenLikeDelete(Like like) {

        log.info("delete like: " + like);
        if (like.getCommentId() != null) {

            communityMapper.decrCommentLikeCount(like.getCommentId());
        } else if (like.getBlogId() != null) {
            communityMapper.decrBlogLikeCount(like.getBlogId());
        }
    }


    private boolean deleteCacheWithRetry(String key, int attempt) {
        if (attempt >= MAX_RETRIES) {
            return false;
        }

        try {
            Boolean result = redisService.removeInfo(key);
            if (Boolean.TRUE.equals(result)) {
                return true;
            } else {
                log.error("Attempt " + (attempt + 1) + ": Failed to delete cache for key: " + key);
                return deleteCacheWithRetry(key, attempt + 1); // 递归重试
            }
        } catch (Exception e) {
            log.error("Attempt " + (attempt + 1) + ": Exception occurred while deleting cache for key: " + key + ", Error: " + e.getMessage());
            return deleteCacheWithRetry(key, attempt + 1); // 递归重试
        }
    }

}

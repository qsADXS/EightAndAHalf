package com.eh.user.listener;

import com.eh.user.mapper.UserMapper;
import com.eh.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserListener {

    private static final int MAX_RETRIES = 3;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "user.queue1"),
            exchange = @Exchange(name = "user.topic",type = ExchangeTypes.TOPIC),key = "user.delete"))
    public void listenUserQueue1(Long id){

        String cacheKey = "user:id:" + id;

        boolean success = deleteCacheWithRetry(cacheKey, 0);
        if (success) {
            log.info("Successfully deleted cache for key: " + cacheKey);
        } else {
            log.error("Failed to delete cache for key: " + cacheKey + " after maximum retries.");
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "user.queue2"),
            exchange = @Exchange(name = "user.topic",type = ExchangeTypes.TOPIC),key = "user.insert"))
    public void listenUserQueue2(User user){
        userMapper.register(user);
    }


    private boolean deleteCacheWithRetry(String cacheKey, int attempt) {
        if (attempt >= MAX_RETRIES) {
            return false; // 达到最大重试次数，返回失败
        }

        try {
            Boolean result = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(result)) {
                return true; // 删除成功
            } else {
                log.error("Attempt " + (attempt + 1) + ": Failed to delete cache for key: " + cacheKey);
                return deleteCacheWithRetry(cacheKey, attempt + 1); // 递归重试
            }
        } catch (Exception e) {
            log.error("Attempt " + (attempt + 1) + ": Exception occurred while deleting cache for key: " + cacheKey + ", Error: " + e.getMessage());
            return deleteCacheWithRetry(cacheKey, attempt + 1); // 递归重试
        }
    }



}

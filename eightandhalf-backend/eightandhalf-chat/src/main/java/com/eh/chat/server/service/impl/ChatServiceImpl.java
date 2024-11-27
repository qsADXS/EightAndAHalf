package com.eh.chat.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.eh.api.client.UserClient;
import com.eh.chat.config.NettyConfig;
import com.eh.chat.mapper.ChatMapper;
import com.eh.chat.model.entity.ChatMessage;
import com.eh.chat.model.request.BlockRequest;
import com.eh.chat.model.request.SendPictureRequest;
import com.eh.chat.server.service.ChatService;
import com.eh.common.entity.User;
import com.eh.common.util.SnowFlakeUtil;
import com.eh.common.util.TencentYunCOSUtil;
import com.eh.common.util.ThreadLocalUtil;
import com.eh.common.vo.PageBean;
import com.eh.common.vo.Result;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserClient userClient;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private ChatMapper chatMapper;

    @Override
    public void saveContent(ChatMessage chatMessage) {
        chatMapper.saveContent(chatMessage);
    }

    @Override
    public Result sendPicture(SendPictureRequest request) {

        Long id = ThreadLocalUtil.get();
        String from = id.toString();
        if(NettyConfig.getChannel(from)==null){
            return Result.error("请先连接websocket...");
        }

        //根据用户名查询用户
        User clientUser = userClient.getUser(request.getTo());
        //判断该用户是否存在
        if (clientUser == null) {
            NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame("该用户不存在"));
            return Result.error("用户不存在");
        }

        String cacheKey = clientUser.getUserId() + ":block:" + from;
        String errorMsg = "用户"+request.getTo()+"屏蔽了你,消息无法发送";

        //判断有没有被屏蔽
        try {
            if((Boolean) redisTemplate.opsForValue().get(cacheKey)){
                NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame(errorMsg));
                return Result.error(errorMsg);
            }
        } catch (Exception e) {
            Boolean isBlocked = isBlocked(clientUser.getUserId(),id);
            log.info(isBlocked.toString());
            if(isBlocked){
                redisTemplate.opsForValue().set(cacheKey, Boolean.TRUE);
                NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame(errorMsg));
                return Result.error(errorMsg);
            }
        }

        String url = TencentYunCOSUtil.upload(request.getPicture());

        Long uniqueChatMessageId= SnowFlakeUtil.getSnowFlakeId();


        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setMessageId(uniqueChatMessageId);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setUserId(id);
        chatMessage.setToUserId(clientUser.getUserId());
        chatMessage.setIsOffline(0);
        chatMessage.setIsTransferMusic(0);
        chatMessage.setMessageContent(url);

        log.info(chatMessage.toString());

        //通知消息写入mysql和redis
        try {
            rabbitTemplate.convertAndSend("online.topic","online",chatMessage);
        } catch (AmqpException e) {
            log.error("在线消息写入数据库或redis失败");
        }

        if(NettyConfig.getChannelMap().containsKey(clientUser.getUserId().toString())) {
            //在线消息
            NettyConfig.getChannel(clientUser.getUserId().toString()).writeAndFlush(new TextWebSocketFrame(url));
        }else {
            //离线消息发送
            //通知离线消息写入redis
            try {
                rabbitTemplate.convertAndSend("offline.topic","offline",chatMessage);
            } catch (AmqpException e) {
                log.error("离线消息写入redis失败");
            }

            NettyConfig.getChannel(from).writeAndFlush(new TextWebSocketFrame(request.getTo()+"未登录,发送离线消息"));
        }
        return Result.success();
    }

    @Override
    public void blockUser(BlockRequest request) {
        Long id = ThreadLocalUtil.get();
        chatMapper.blockUser(id,request.getId());
    }

    @Override
    public Boolean isBlocked(Long to, Long from) {
        int res = chatMapper.isBlocked(to, from);
        return res == 1;
    }

    @Override
    public PageBean<ChatMessage> getMessageRecord(Long id, Long userId, String date, Integer pageNum, Integer pageSize) {

        PageBean<ChatMessage> pageBean = new PageBean<>();

        if(pageNum!=null&&pageSize!=null)
            PageHelper.startPage(pageNum, pageSize);
        else
            PageHelper.startPage(1, 2000);


        List<ChatMessage> chatMessages = chatMapper.getMessageRecord(id, userId, date, null);

        Page<ChatMessage> chatMessagePage = (Page<ChatMessage>) chatMessages;
        pageBean.setItems(chatMessagePage.getResult());
        pageBean.setTotal(chatMessagePage.getTotal());
        return pageBean;
    }



    @Override
    public void updateOfflineMsg(Long id) {
        chatMapper.updateOfflineMsg(id);
    }

    @Override
    public List<ChatMessage> getOfflineMessage(Long id, int i) {
        return chatMapper.getOfflineMessage(id,i);
    }
}

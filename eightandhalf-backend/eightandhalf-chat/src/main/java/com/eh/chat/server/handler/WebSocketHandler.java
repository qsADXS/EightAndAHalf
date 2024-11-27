package com.eh.chat.server.handler;


import com.alibaba.fastjson.JSON;
import com.eh.api.client.UserClient;
import com.eh.chat.config.NettyConfig;
import com.eh.chat.model.entity.ChatMessage;
import com.eh.chat.server.NettyServer;
import com.eh.chat.server.service.ChatService;
import com.eh.common.dto.UserDTO;
import com.eh.common.entity.User;
import com.eh.common.util.JwtUtil;
import com.eh.common.util.SnowFlakeUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    @Autowired
    private UserClient userClient;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ChatService chatService;

    /**
     * 读取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        //获取发送者的名称
        AttributeKey<String> key = AttributeKey.valueOf("id");
        Long id = Long.valueOf(ctx.channel().attr(key).get());
        //检查是否有离线消息

        List<ChatMessage> offlineMessage = chatService.getOfflineMessage(id, 1);
        log.info("离线消息"+offlineMessage.toString());
        if (!offlineMessage.isEmpty()) {
            //ctx.channel().writeAndFlush(new TextWebSocketFrame(offlineMessage.toString()));
            //通知消息更新数据库
            try {
                rabbitTemplate.convertAndSend("msg.topic","msg.update",id);
            } catch (AmqpException e) {
                log.error("在线消息写入数据库或redis失败");
            }
        }

        log.info("2222");
        //接收的消息
        Map map = JSON.parseObject(msg.text(), Map.class);
        String type = map.get("type").toString();
        switch (type){
            case "1": // 发送对话消息
                send(map,ctx);
                break;
        }
       log.info(String.format("收到客户端%s的数据：%s", ctx.channel().id(), msg.text()));
    }


    private void send(Map map,ChannelHandlerContext ctx) {

        //获取发送者的名称
        AttributeKey<String> key = AttributeKey.valueOf("id");
        Long from = Long.valueOf(ctx.channel().attr(key).get());


        String to =map.get("to").toString();

        //根据用户名查询用户
        User clientUser = userClient.getUser(to);
        if (clientUser == null) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("该用户不存在"));
            return;
        }
        log.info(clientUser.toString());


        String cacheKey = clientUser.getUserId() + ":block:" + from;

        //判断有没有被屏蔽
        try {
            if((Boolean) redisTemplate.opsForValue().get(cacheKey)){
                ctx.channel().writeAndFlush(new TextWebSocketFrame("用户"+to+"屏蔽了你,消息无法发送"));
                return;
            }
        } catch (Exception e) {
           Boolean isBlocked = chatService.isBlocked(clientUser.getUserId(),from);
           log.info(isBlocked.toString());
           if(isBlocked){
               redisTemplate.opsForValue().set(cacheKey, Boolean.TRUE);
               ctx.channel().writeAndFlush(new TextWebSocketFrame("用户"+to+"屏蔽了你,消息无法发送"));
               return;
           }
        }

        log.info("block排除");

        String content = map.get("content").toString();

        Long uniqueChatMessageId= SnowFlakeUtil.getSnowFlakeId();

        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setMessageId(uniqueChatMessageId);
        chatMessage.setMessageContent(content);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setUserId(from);
        chatMessage.setToUserId(clientUser.getUserId());
        chatMessage.setIsTransferMusic(0);
        chatMessage.setIsOffline(0);

        log.info("对象创建排查");

        if(NettyConfig.getChannelMap().containsKey(clientUser.getUserId().toString())) {
            //在线消息
            NettyConfig.getChannel(clientUser.getUserId().toString()).writeAndFlush(new TextWebSocketFrame(content));
            //通知消息写入mysql和redis
            try {
                rabbitTemplate.convertAndSend("online.topic","online",chatMessage);
                log.info("通知排查");
            } catch (AmqpException e) {
                log.error("在线消息写入数据库或redis失败");
            }
        }else {
            //离线消息发送
            //通知离线消息写入redis
            try {
                chatMessage.setIsOffline(1);
                rabbitTemplate.convertAndSend("offline.topic","offline",chatMessage);
            } catch (AmqpException e) {
                log.error("离线消息写入redis失败");
            }

            //ctx.channel().writeAndFlush(new TextWebSocketFrame(to+"未登录,发送离线消息"));
        }

    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("用户下线了:{}", ctx.channel().id().asLongText());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常：{}", cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     */
    private void removeUserId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf("id");
        String id = ctx.channel().attr(key).get();
        NettyConfig.getChannelMap().remove(id);
    }
}

package com.eh.chat.listener;

import com.eh.chat.model.entity.ChatMessage;
import com.eh.chat.server.NettyServer;
import com.eh.chat.server.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatListener {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private ChatService chatService;



    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "offline.queue"),
            exchange = @Exchange(name = "offline.topic",type = ExchangeTypes.TOPIC),key = "offline"))
    public void listenOfflineQueue(ChatMessage chatMessage){
        chatMessage.setIsOffline(1);
        log.info("监听到:"+chatMessage);
        chatService.saveContent(chatMessage);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "online.queue"),
            exchange = @Exchange(name = "online.topic",type = ExchangeTypes.TOPIC),key = "online"))
    public void listenOnlineQueue(ChatMessage chatMessage){
        //保存到数据库
        chatService.saveContent(chatMessage);
        log.info(chatMessage.toString());
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "msg.update.queue"),
            exchange = @Exchange(name = "msg.topic",type = ExchangeTypes.TOPIC),key = "msg.update"))
    public void listenMsgUpdateQueue(Long id){
        //保存到数据库
        chatService.updateOfflineMsg(id);

    }


}

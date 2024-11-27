package com.eh.chat.controller;

import com.eh.chat.config.NettyConfig;
import com.eh.chat.model.entity.ChatMessage;
import com.eh.chat.model.request.BlockRequest;
import com.eh.chat.model.request.SendPictureRequest;
import com.eh.chat.server.service.ChatService;
import com.eh.common.util.ThreadLocalUtil;
import com.eh.common.vo.PageBean;
import com.eh.common.vo.Result;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/image")
    public Result sendPicture(@ModelAttribute @Validated SendPictureRequest request) {
        return chatService.sendPicture(request);
    }

    @PostMapping("/block")
    public Result blockUser(@ModelAttribute @Validated BlockRequest request){
        chatService.blockUser(request);
        return Result.success();
    }

    @GetMapping("/history")
    public Result<PageBean<ChatMessage>> getMessageRecord(Long userId, @RequestParam(required = false) String date,Integer pageNum, Integer pageSize){
        Long id = ThreadLocalUtil.get();
        PageBean<ChatMessage> chatMessages = chatService.getMessageRecord(id,userId,date,pageNum,pageSize);
        return Result.success(chatMessages);
    }

}

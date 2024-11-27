package com.eh.chat.server.service;

import com.eh.chat.model.entity.ChatMessage;
import com.eh.chat.model.request.BlockRequest;
import com.eh.chat.model.request.SendPictureRequest;
import com.eh.common.vo.PageBean;
import com.eh.common.vo.Result;

import java.util.List;

public interface ChatService {
    void saveContent(ChatMessage chatMessage);

    Result sendPicture(SendPictureRequest request);

    void blockUser(BlockRequest request);

    Boolean isBlocked(Long to, Long from);

    PageBean<ChatMessage> getMessageRecord(Long id, Long userId, String date, Integer pageNum, Integer pageSize);

    void updateOfflineMsg(Long id);

    List<ChatMessage> getOfflineMessage(Long id, int i);
}

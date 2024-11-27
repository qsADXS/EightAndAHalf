package com.eh.chat.mapper;

import com.eh.chat.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@Mapper
public interface ChatMapper {
    void saveContent(ChatMessage chatMessage);

    void blockUser(Long id, Long blockId);

    int isBlocked(Long to, Long from);

    List<ChatMessage> getOfflineMessage(Long id,Integer isOffline);

    List<ChatMessage> getMessageRecord(Long id, Long userId, String date, Integer isOffline);

    void updateOfflineMsg(Long id);
}

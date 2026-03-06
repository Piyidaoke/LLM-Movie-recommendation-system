package com.example.movierecommender.mapper;

import com.example.movierecommender.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatMessageMapper {
    List<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId);
    List<ChatMessage> findRecentByConversationId(@Param("conversationId") Long conversationId,
                                                  @Param("limit") int limit);
    int insert(ChatMessage message);
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}

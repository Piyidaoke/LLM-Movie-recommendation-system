package com.example.movierecommender.mapper;

import com.example.movierecommender.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ConversationMapper {
    Conversation findById(@Param("conversationId") Long conversationId);
    List<Conversation> findByUserId(@Param("userId") Long userId);
    int insert(Conversation conversation);
    int updateTitle(@Param("conversationId") Long conversationId, @Param("title") String title);
    int deleteById(@Param("conversationId") Long conversationId);
}

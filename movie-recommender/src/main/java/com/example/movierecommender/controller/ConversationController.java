package com.example.movierecommender.controller;

import com.example.movierecommender.common.Result;
import com.example.movierecommender.entity.ChatMessage;
import com.example.movierecommender.entity.Conversation;
import com.example.movierecommender.mapper.ChatMessageMapper;
import com.example.movierecommender.mapper.ConversationMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ConversationController(ConversationMapper conversationMapper,
                                   ChatMessageMapper chatMessageMapper) {
        this.conversationMapper = conversationMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @GetMapping
    public Result<List<Conversation>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(conversationMapper.findByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Conversation conv = conversationMapper.findById(id);
        Long userId = (Long) auth.getPrincipal();
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new IllegalArgumentException("会话不存在或无权限");
        }
        chatMessageMapper.deleteByConversationId(id);
        conversationMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}/messages")
    public Result<List<ChatMessage>> messages(Authentication auth, @PathVariable Long id) {
        Conversation conv = conversationMapper.findById(id);
        Long userId = (Long) auth.getPrincipal();
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new IllegalArgumentException("会话不存在或无权限");
        }
        return Result.success(chatMessageMapper.findByConversationId(id));
    }
}

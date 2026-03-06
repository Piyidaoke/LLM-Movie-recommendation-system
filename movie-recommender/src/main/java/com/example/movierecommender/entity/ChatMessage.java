package com.example.movierecommender.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long messageId;
    private Long conversationId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}

package com.example.movierecommender.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Conversation {
    private Long conversationId;
    private Long userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

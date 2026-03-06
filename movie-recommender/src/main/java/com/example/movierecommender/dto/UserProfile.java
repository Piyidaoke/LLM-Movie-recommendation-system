package com.example.movierecommender.dto;

import lombok.Data;

@Data
public class UserProfile {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
}

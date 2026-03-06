package com.example.movierecommender.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long favoriteId;
    private Long userId;
    private Long movieId;
    private LocalDateTime createdAt;
}

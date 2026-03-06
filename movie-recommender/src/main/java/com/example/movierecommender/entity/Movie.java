package com.example.movierecommender.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Movie {
    private Long movieId;
    private Integer tmdbId;
    private String title;
    private String overview;
    private String genres;
    private String director;
    private String castMembers;
    private LocalDate releaseDate;
    private String posterUrl;
    private BigDecimal voteAverage;
    private String embeddingText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

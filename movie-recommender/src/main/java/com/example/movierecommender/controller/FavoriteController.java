package com.example.movierecommender.controller;

import com.example.movierecommender.common.Result;
import com.example.movierecommender.entity.Favorite;
import com.example.movierecommender.entity.Movie;
import com.example.movierecommender.mapper.FavoriteMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;

    public FavoriteController(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    @PostMapping
    public Result<Void> add(Authentication auth, @RequestParam Long movieId) {
        Long userId = (Long) auth.getPrincipal();
        if (favoriteMapper.exists(userId, movieId)) {
            throw new IllegalArgumentException("已收藏该电影");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setMovieId(movieId);
        favoriteMapper.insert(fav);
        return Result.success();
    }

    @DeleteMapping("/{movieId}")
    public Result<Void> remove(Authentication auth, @PathVariable Long movieId) {
        Long userId = (Long) auth.getPrincipal();
        favoriteMapper.deleteByUserIdAndMovieId(userId, movieId);
        return Result.success();
    }

    @GetMapping
    public Result<List<Movie>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(favoriteMapper.findMoviesByUserId(userId));
    }
}

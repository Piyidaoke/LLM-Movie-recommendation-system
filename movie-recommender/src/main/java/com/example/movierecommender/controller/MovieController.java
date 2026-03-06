package com.example.movierecommender.controller;

import com.example.movierecommender.common.Result;
import com.example.movierecommender.entity.Movie;
import com.example.movierecommender.mapper.MovieMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieMapper movieMapper;

    public MovieController(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    @GetMapping("/{id}")
    public Result<Movie> getById(@PathVariable Long id) {
        Movie movie = movieMapper.findById(id);
        if (movie == null) throw new IllegalArgumentException("电影不存在");
        return Result.success(movie);
    }
}

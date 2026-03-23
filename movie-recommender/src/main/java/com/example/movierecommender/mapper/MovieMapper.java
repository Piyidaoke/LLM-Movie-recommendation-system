package com.example.movierecommender.mapper;

import com.example.movierecommender.entity.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MovieMapper {
    Movie findById(@Param("movieId") Long movieId);
    Movie findByTmdbId(@Param("tmdbId") Integer tmdbId);
    List<Movie> findAll();
    List<Movie> findByIds(@Param("movieIds") List<Long> movieIds);
    int insert(Movie movie);
    int batchInsert(@Param("movies") List<Movie> movies);
    int upsert(Movie movie);
    List<Movie> searchByTitle(@Param("keyword") String keyword);
    long count();
}

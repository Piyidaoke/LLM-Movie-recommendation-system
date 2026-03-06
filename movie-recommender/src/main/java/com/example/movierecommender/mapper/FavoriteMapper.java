package com.example.movierecommender.mapper;

import com.example.movierecommender.entity.Favorite;
import com.example.movierecommender.entity.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FavoriteMapper {
    int insert(Favorite favorite);
    int deleteByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);
    List<Movie> findMoviesByUserId(@Param("userId") Long userId);
    boolean exists(@Param("userId") Long userId, @Param("movieId") Long movieId);
}

package com.example.movierecommender.mapper;

import com.example.movierecommender.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);
    User findById(@Param("userId") Long userId);
    int insert(User user);
    int update(User user);
}

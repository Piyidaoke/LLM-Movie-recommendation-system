package com.example.movierecommender.service;

import com.example.movierecommender.dto.*;
import com.example.movierecommender.entity.User;
import com.example.movierecommender.mapper.UserMapper;
import com.example.movierecommender.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void register(RegisterRequest req) {
        if (userMapper.findByUsername(req.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userMapper.findByEmail(req.getEmail()) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUsername());
        return new LoginResponse(token, user.getUserId(), user.getUsername());
    }

    public UserProfile getProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getUserId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setAvatarUrl(user.getAvatarUrl());
        return profile;
    }

    public void updateProfile(Long userId, UserProfile req) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(req.getUsername());
        user.setAvatarUrl(req.getAvatarUrl());
        userMapper.update(user);
    }
}

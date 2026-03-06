package com.example.movierecommender.controller;

import com.example.movierecommender.common.Result;
import com.example.movierecommender.dto.UserProfile;
import com.example.movierecommender.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<UserProfile> getProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody UserProfile req) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateProfile(userId, req);
        return Result.success();
    }
}

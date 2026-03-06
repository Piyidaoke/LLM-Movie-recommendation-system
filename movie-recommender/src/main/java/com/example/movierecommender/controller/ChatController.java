package com.example.movierecommender.controller;

import com.example.movierecommender.service.RecommendationService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RecommendationService recommendationService;

    public ChatController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(Authentication auth,
                                    @RequestParam(required = false) Long conversationId,
                                    @RequestParam String message) {
        Long userId = (Long) auth.getPrincipal();
        return recommendationService.streamChat(userId, conversationId, message);
    }
}

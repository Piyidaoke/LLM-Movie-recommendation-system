package com.example.movierecommender.controller;

import com.example.movierecommender.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public ChatController(RecommendationService recommendationService, ObjectMapper objectMapper) {
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(Authentication auth,
                                  @RequestParam(required = false) Long conversationId,
                                  @RequestParam String message) {
        Long userId = (Long) auth.getPrincipal();
        SseEmitter emitter = new SseEmitter(300_000L);

        RecommendationService.ChatStreamResult result =
                recommendationService.streamChat(userId, conversationId, message);

        // 先发送电影数据（连接刚建立时是稳定的，结尾可能断开）
        try {
            if (!result.recommendedMovies().isEmpty()) {
                String moviesJson = objectMapper.writeValueAsString(result.recommendedMovies());
                emitter.send(SseEmitter.event().name("movies").data(moviesJson));
            }
        } catch (Exception e) {
            emitter.completeWithError(e);
            return emitter;
        }

        result.stream().subscribe(
                token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                () -> {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
        );

        return emitter;
    }
}

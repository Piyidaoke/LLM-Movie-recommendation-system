package com.example.movierecommender.service;

import com.example.movierecommender.entity.ChatMessage;
import com.example.movierecommender.entity.Conversation;
import com.example.movierecommender.entity.Movie;
import com.example.movierecommender.mapper.ChatMessageMapper;
import com.example.movierecommender.mapper.ConversationMapper;
import com.example.movierecommender.mapper.MovieMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个专业的电影推荐助手。请根据以下电影数据库中的电影信息回答用户问题。
            优先推荐数据库中的电影，并附上真实的评分和详细信息。

            ## 电影数据库中的相关电影：
            %s

            ## 回复要求：
            - 使用 Markdown 格式，电影名称请用《》包裹
            - 每部推荐的电影包含：《标题》、评分、类型、上映年份、推荐理由
            - 推荐理由要结合用户的具体需求来写
            - 来自数据库的电影请标注 [数据库]，来自你自身知识的电影请标注 [站外推荐]
            - 优先推荐数据库中的电影，数据库不足时再补充站外推荐
            - 如果用户的问题与电影无关，友好地引导用户提出电影相关的问题
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final MovieMapper movieMapper;

    public RecommendationService(ChatClient.Builder chatClientBuilder,
                                  VectorStore vectorStore,
                                  ConversationMapper conversationMapper,
                                  ChatMessageMapper chatMessageMapper,
                                  MovieMapper movieMapper) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.conversationMapper = conversationMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.movieMapper = movieMapper;
    }

    public record ChatStreamResult(Flux<String> stream, List<Movie> recommendedMovies) {}

    public ChatStreamResult streamChat(Long userId, Long conversationId, String userMessage) {
        // 1. 如果没有 conversationId，创建新会话
        if (conversationId == null) {
            Conversation conv = new Conversation();
            conv.setUserId(userId);
            conv.setTitle(userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
            conversationMapper.insert(conv);
            conversationId = conv.getConversationId();
        }

        // 2. 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        chatMessageMapper.insert(userMsg);

        // 3. 向量检索（带容错）
        String context;
        List<Movie> matchedMovies = new ArrayList<>();
        try {
            List<Document> docs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(userMessage)
                            .topK(8)
                            .similarityThreshold(0.2)
                            .build());
            log.info("向量检索到 {} 条相关电影", docs.size());
            context = docs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n---\n\n"));
            if (context.isEmpty()) {
                context = "（数据库中未检索到相关电影，请根据你的专业知识回答用户的电影相关问题）";
            }

            // 提取匹配的电影完整信息
            List<Long> movieIds = docs.stream()
                    .map(doc -> doc.getMetadata().get("movieId"))
                    .filter(Objects::nonNull)
                    .map(id -> Long.valueOf(id.toString()))
                    .distinct()
                    .collect(Collectors.toList());
            if (!movieIds.isEmpty()) {
                matchedMovies = movieMapper.findByIds(movieIds);
            }
        } catch (Exception e) {
            log.warn("向量检索失败，将跳过 RAG 上下文: {}", e.getMessage());
            context = "（向量检索暂时不可用，请根据你的专业知识回答用户的电影相关问题）";
        }

        // 4. 加载历史消息
        List<ChatMessage> history = chatMessageMapper.findRecentByConversationId(conversationId, 10);
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(String.format(SYSTEM_PROMPT_TEMPLATE, context)));
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 5. 流式调用 LLM
        final Long convId = conversationId;
        StringBuilder fullResponse = new StringBuilder();

        Flux<String> flux = chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    // 6. 保存 assistant 消息
                    ChatMessage assistantMsg = new ChatMessage();
                    assistantMsg.setConversationId(convId);
                    assistantMsg.setRole("assistant");
                    assistantMsg.setContent(fullResponse.toString());
                    chatMessageMapper.insert(assistantMsg);
                });

        return new ChatStreamResult(flux, matchedMovies);
    }
}

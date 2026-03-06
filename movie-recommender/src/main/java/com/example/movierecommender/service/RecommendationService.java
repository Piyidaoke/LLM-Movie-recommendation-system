package com.example.movierecommender.service;

import com.example.movierecommender.entity.ChatMessage;
import com.example.movierecommender.entity.Conversation;
import com.example.movierecommender.mapper.ChatMessageMapper;
import com.example.movierecommender.mapper.ConversationMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个专业的电影推荐助手。请仅根据以下电影信息回答用户问题。
            如果提供的电影中没有与用户需求相关的内容，请诚实地说"抱歉，我没有找到完全匹配的电影，但以下电影可能你会感兴趣"。

            ## 可供推荐的电影：
            %s

            ## 回复要求：
            - 使用 Markdown 格式
            - 每部推荐的电影包含：**标题**、评分、类型、上映年份、推荐理由
            - 推荐理由要结合用户的具体需求来写
            - 如果用户的问题与电影无关，友好地引导用户提出电影相关的问题
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    public RecommendationService(ChatClient.Builder chatClientBuilder,
                                  VectorStore vectorStore,
                                  ConversationMapper conversationMapper,
                                  ChatMessageMapper chatMessageMapper) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.conversationMapper = conversationMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    public Flux<String> streamChat(Long userId, Long conversationId, String userMessage) {
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

        // 3. 向量检索
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userMessage)
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build());

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isEmpty()) {
            context = "（未检索到相关电影信息）";
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

        return chatClient.prompt()
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
    }
}

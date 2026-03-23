package com.example.movierecommender.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class EmbeddingConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingConfig.class);

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${app.embedding.api-key}") String apiKey,
            @Value("${app.embedding.base-url}") String baseUrl,
            @Value("${app.embedding.model}") String model) {
        return new DashScopeEmbeddingModel(apiKey, baseUrl, model);
    }

    /**
     * 自定义 EmbeddingModel，直接调用 DashScope OpenAI 兼容接口，
     * 避免 Spring AI OpenAiApi 的 URL 拼接问题。
     */
    static class DashScopeEmbeddingModel implements EmbeddingModel {

        private final String apiKey;
        private final String embeddingUrl;
        private final String model;
        private final RestTemplate restTemplate = new RestTemplate();
        private final ObjectMapper objectMapper = new ObjectMapper();

        DashScopeEmbeddingModel(String apiKey, String baseUrl, String model) {
            this.apiKey = apiKey;
            // 确保 URL 正确: baseUrl/embeddings
            this.embeddingUrl = baseUrl.endsWith("/")
                    ? baseUrl + "embeddings"
                    : baseUrl + "/embeddings";
            this.model = model;
            log.info("DashScope Embedding URL: {}", this.embeddingUrl);
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<String> inputs = request.getInstructions();
            List<Embedding> allEmbeddings = new ArrayList<>();

            // DashScope 限制每批最多 10 条
            int batchSize = 10;
            for (int i = 0; i < inputs.size(); i += batchSize) {
                List<String> batch = inputs.subList(i, Math.min(i + batchSize, inputs.size()));
                List<Embedding> batchResult = callApi(batch, i);
                allEmbeddings.addAll(batchResult);
            }

            return new EmbeddingResponse(allEmbeddings);
        }

        private List<Embedding> callApi(List<String> texts, int indexOffset) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "input", texts,
                    "dimensions", 1024
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<String> resp = restTemplate.exchange(
                        embeddingUrl, HttpMethod.POST, entity, String.class);
                JsonNode root = objectMapper.readTree(resp.getBody());
                JsonNode dataArray = root.get("data");

                List<Embedding> embeddings = new ArrayList<>();
                for (JsonNode item : dataArray) {
                    JsonNode embNode = item.get("embedding");
                    float[] vector = new float[embNode.size()];
                    for (int j = 0; j < embNode.size(); j++) {
                        vector[j] = (float) embNode.get(j).asDouble();
                    }
                    embeddings.add(new Embedding(vector, indexOffset + item.get("index").asInt()));
                }
                return embeddings;
            } catch (Exception e) {
                log.error("DashScope embedding 调用失败: {}", e.getMessage());
                throw new RuntimeException("Embedding API 调用失败", e);
            }
        }

        @Override
        public float[] embed(org.springframework.ai.document.Document document) {
            EmbeddingResponse resp = call(new EmbeddingRequest(List.of(document.getText()), null));
            return resp.getResult().getOutput();
        }

        @Override
        public int dimensions() {
            return 1024;
        }
    }
}

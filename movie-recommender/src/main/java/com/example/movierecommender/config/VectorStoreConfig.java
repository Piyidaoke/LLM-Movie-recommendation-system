package com.example.movierecommender.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Bean
    public JedisPooled jedisPooled(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        return new JedisPooled(host, port);
    }

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        // Manually create the Redis index to avoid the embedding API probe during startup
        createIndexIfNotExists(jedisPooled);

        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("movie_vector_index")
                .prefix("movie_vector:")
                .initializeSchema(false)
                .build();
    }

    private void createIndexIfNotExists(JedisPooled jedisPooled) {
        try {
            jedisPooled.ftInfo("movie_vector_index");
            log.info("Redis vector index 'movie_vector_index' already exists.");
        } catch (Exception e) {
            try {
                Map<String, Object> vectorAttrs = new HashMap<>();
                vectorAttrs.put("TYPE", "FLOAT32");
                vectorAttrs.put("DIM", 1024);
                vectorAttrs.put("DISTANCE_METRIC", "COSINE");

                Schema schema = new Schema()
                        .addTextField("content", 1.0)
                        .addVectorField("embedding",
                                Schema.VectorField.VectorAlgo.FLAT, vectorAttrs);

                IndexDefinition def = new IndexDefinition(IndexDefinition.Type.HASH)
                        .setPrefixes(new String[]{"movie_vector:"});

                jedisPooled.ftCreate("movie_vector_index",
                        IndexOptions.defaultOptions().setDefinition(def), schema);
                log.info("Created Redis vector index 'movie_vector_index' with 1024 dimensions.");
            } catch (Exception ex) {
                log.warn("Failed to create Redis vector index: {}", ex.getMessage());
            }
        }
    }
}

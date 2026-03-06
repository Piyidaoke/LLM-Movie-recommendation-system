package com.example.movierecommender.service;

import com.example.movierecommender.entity.Movie;
import com.example.movierecommender.mapper.MovieMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    private final TmdbService tmdbService;
    private final MovieMapper movieMapper;
    private final VectorStore vectorStore;

    public DataIngestionService(TmdbService tmdbService, MovieMapper movieMapper,
                                VectorStore vectorStore) {
        this.tmdbService = tmdbService;
        this.movieMapper = movieMapper;
        this.vectorStore = vectorStore;
    }

    public int fullIngest(int startYear, int endYear, int maxPagesPerYear) {
        int total = 0;
        for (int year = endYear; year >= startYear; year--) {
            log.info("采集 {} 年电影...", year);
            List<Movie> movies = tmdbService.fetchMoviesByYear(year, maxPagesPerYear);

            for (Movie movie : movies) {
                if (movieMapper.findByTmdbId(movie.getTmdbId()) != null) continue;

                tmdbService.enrichCredits(movie);
                tmdbService.buildEmbeddingText(movie);
                movieMapper.upsert(movie);
                total++;
            }
            log.info("{} 年采集完成，累计: {} 部", year, total);
        }

        log.info("全量采集完成，共 {} 部电影", total);
        return total;
    }

    public int vectorizeAll() {
        List<Movie> movies = movieMapper.findAll();
        List<Document> documents = new ArrayList<>();

        for (Movie movie : movies) {
            if (movie.getEmbeddingText() == null || movie.getEmbeddingText().isEmpty()) continue;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("movieId", movie.getMovieId());
            metadata.put("tmdbId", movie.getTmdbId());
            metadata.put("title", movie.getTitle());
            metadata.put("voteAverage", movie.getVoteAverage().doubleValue());

            Document doc = new Document(movie.getEmbeddingText(), metadata);
            documents.add(doc);
        }

        int batchSize = 100;
        for (int i = 0; i < documents.size(); i += batchSize) {
            List<Document> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
            vectorStore.add(batch);
            log.info("向量化进度: {}/{}", Math.min(i + batchSize, documents.size()), documents.size());
        }

        log.info("向量化完成，共 {} 条", documents.size());
        return documents.size();
    }
}

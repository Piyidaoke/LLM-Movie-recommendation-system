package com.example.movierecommender.service;

import com.example.movierecommender.entity.Movie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TmdbService {

    private static final Logger log = LoggerFactory.getLogger(TmdbService.class);

    // TMDB genre ID -> 中文名称映射
    private static final Map<Integer, String> GENRE_MAP = Map.ofEntries(
            Map.entry(28, "动作"), Map.entry(12, "冒险"), Map.entry(16, "动画"),
            Map.entry(35, "喜剧"), Map.entry(80, "犯罪"), Map.entry(99, "纪录片"),
            Map.entry(18, "剧情"), Map.entry(10751, "家庭"), Map.entry(14, "奇幻"),
            Map.entry(36, "历史"), Map.entry(27, "恐怖"), Map.entry(10402, "音乐"),
            Map.entry(9648, "悬疑"), Map.entry(10749, "爱情"), Map.entry(878, "科幻"),
            Map.entry(10770, "电视电影"), Map.entry(53, "惊悚"), Map.entry(10752, "战争"),
            Map.entry(37, "西部")
    );

    @Value("${app.tmdb.api-key}")
    private String apiKey;

    @Value("${app.tmdb.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public List<Movie> fetchMoviesByYear(int year, int maxPages) {
        List<Movie> movies = new ArrayList<>();
        for (int page = 1; page <= maxPages; page++) {
            String url = baseUrl + "/discover/movie?api_key=" + apiKey
                    + "&language=zh-CN&sort_by=popularity.desc"
                    + "&primary_release_year=" + year + "&page=" + page;
            try {
                ResponseEntity<String> resp = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), String.class);
                JsonNode root = objectMapper.readTree(resp.getBody());
                JsonNode results = root.get("results");
                if (results == null || results.isEmpty()) break;

                int totalPages = root.get("total_pages").asInt();
                for (JsonNode node : results) {
                    movies.add(parseMovie(node));
                }
                if (page >= totalPages) break;

                Thread.sleep(250);
            } catch (Exception e) {
                log.error("获取电影失败: year={}, page={}", year, page, e);
                break;
            }
        }
        return movies;
    }

    public void enrichCredits(Movie movie) {
        String url = baseUrl + "/movie/" + movie.getTmdbId() + "/credits?api_key=" + apiKey + "&language=zh-CN";
        try {
            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());

            JsonNode crew = root.get("crew");
            if (crew != null) {
                for (JsonNode c : crew) {
                    if ("Director".equals(c.get("job").asText())) {
                        movie.setDirector(c.get("name").asText());
                        break;
                    }
                }
            }

            JsonNode cast = root.get("cast");
            if (cast != null) {
                List<String> castNames = new ArrayList<>();
                for (int i = 0; i < Math.min(5, cast.size()); i++) {
                    castNames.add(cast.get(i).get("name").asText());
                }
                movie.setCastMembers(objectMapper.writeValueAsString(castNames));
            }

            Thread.sleep(250);
        } catch (Exception e) {
            log.error("获取演职人员失败: tmdbId={}", movie.getTmdbId(), e);
        }
    }

    private Movie parseMovie(JsonNode node) {
        Movie movie = new Movie();
        movie.setTmdbId(node.get("id").asInt());
        movie.setTitle(node.get("title").asText());
        movie.setOverview(node.has("overview") ? node.get("overview").asText() : "");
        movie.setVoteAverage(BigDecimal.valueOf(node.get("vote_average").asDouble()));
        movie.setPosterUrl(node.has("poster_path") && !node.get("poster_path").isNull()
                ? "https://image.tmdb.org/t/p/w500" + node.get("poster_path").asText() : null);

        String releaseDateStr = node.has("release_date") ? node.get("release_date").asText() : "";
        if (!releaseDateStr.isEmpty()) {
            try { movie.setReleaseDate(LocalDate.parse(releaseDateStr)); } catch (Exception ignored) {}
        }

        // 将 genre ID 转换为中文名称
        JsonNode genreIds = node.get("genre_ids");
        if (genreIds != null) {
            List<String> genreNames = new ArrayList<>();
            for (JsonNode g : genreIds) {
                String name = GENRE_MAP.get(g.asInt());
                if (name != null) genreNames.add(name);
            }
            movie.setGenres(String.join("/", genreNames));
        }

        return movie;
    }

    public void buildEmbeddingText(Movie movie) {
        StringBuilder sb = new StringBuilder();
        sb.append(movie.getTitle()).append("。");
        if (movie.getGenres() != null) sb.append("类型: ").append(movie.getGenres()).append("。");
        if (movie.getOverview() != null && !movie.getOverview().isEmpty()) {
            sb.append("简介: ").append(movie.getOverview()).append("。");
        }
        if (movie.getDirector() != null) sb.append("导演: ").append(movie.getDirector()).append("。");
        if (movie.getCastMembers() != null) sb.append("主演: ").append(movie.getCastMembers()).append("。");
        if (movie.getReleaseDate() != null) sb.append("上映: ").append(movie.getReleaseDate()).append("。");
        sb.append("评分: ").append(movie.getVoteAverage());
        movie.setEmbeddingText(sb.toString());
    }
}

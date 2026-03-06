package com.example.movierecommender.controller;

import com.example.movierecommender.common.Result;
import com.example.movierecommender.service.DataIngestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DataIngestionService dataIngestionService;

    public AdminController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    @PostMapping("/ingest")
    public Result<String> ingest(@RequestParam(defaultValue = "2000") int startYear,
                                  @RequestParam(defaultValue = "2025") int endYear,
                                  @RequestParam(defaultValue = "15") int maxPages) {
        int count = dataIngestionService.fullIngest(startYear, endYear, maxPages);
        return Result.success("采集完成，共 " + count + " 部电影");
    }

    @PostMapping("/vectorize")
    public Result<String> vectorize() {
        int count = dataIngestionService.vectorizeAll();
        return Result.success("向量化完成，共 " + count + " 条");
    }
}

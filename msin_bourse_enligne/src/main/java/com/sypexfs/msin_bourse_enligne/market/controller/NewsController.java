package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.dto.NewsDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketNews;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/market/news")
@RequiredArgsConstructor
public class NewsController {

    private final MarketService marketService;
    private final MarketMapper marketMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsDto>>> getRecentNews(
            @RequestParam(defaultValue = "20") int limit) {
        List<MarketNews> news = marketService.getRecentNews(limit);
        List<NewsDto> dtos = marketMapper.toNewsDtoList(news);
        return ResponseEntity.ok(ApiResponse.success(dtos, "News retrieved successfully"));
    }

    @GetMapping("/urgency/{urgency}")
    public ResponseEntity<ApiResponse<List<NewsDto>>> getNewsByUrgency(
            @PathVariable String urgency,
            @RequestParam(defaultValue = "20") int limit) {
        List<MarketNews> news = marketService.getNewsByUrgency(urgency, limit);
        List<NewsDto> dtos = marketMapper.toNewsDtoList(news);
        return ResponseEntity.ok(ApiResponse.success(dtos, "News by urgency retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<NewsDto>>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        List<MarketNews> news = marketService.searchNews(keyword, limit);
        List<NewsDto> dtos = marketMapper.toNewsDtoList(news);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Search results retrieved successfully"));
    }
}

package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.dto.IndexDto;
import com.sypexfs.msin_bourse_enligne.market.dto.IndexSummaryDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketIndexResponseDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketIndex;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketIndexSummary;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/market/indices")
@RequiredArgsConstructor
public class IndexController {

    private final MarketService marketService;
    private final MarketMapper marketMapper;

    @GetMapping
    public ResponseEntity<Map<String, List<MarketIndexResponseDto>>> getAllIndices() {
        List<MarketIndex> indices = marketService.getAllIndices();
        List<MarketIndexResponseDto> dtos = marketMapper.toMarketIndexResponseDtoList(indices);
        
        // Group by indexId (code)
        Map<String, List<MarketIndexResponseDto>> groupedIndices = dtos.stream()
                .collect(Collectors.groupingBy(MarketIndexResponseDto::getIndexId));
        
        return ResponseEntity.ok(groupedIndices);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<IndexDto>> getIndexByCode(@PathVariable String code) {
        return marketService.getLatestIndexByCode(code)
                .map(marketMapper::toIndexDto)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Index retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{indexType}")
    public ResponseEntity<ApiResponse<List<IndexDto>>> getIndicesByType(@PathVariable String indexType) {
        List<MarketIndex> indices = marketService.getIndicesByType(indexType);
        List<IndexDto> dtos = marketMapper.toIndexDtoList(indices);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Indices by type retrieved successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<IndexSummaryDto>>> getAllIndexSummaries() {
        List<MarketIndexSummary> summaries = marketService.getAllIndexSummaries();
        List<IndexSummaryDto> dtos = marketMapper.toIndexSummaryDtoList(summaries);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Index summaries retrieved successfully"));
    }

    @GetMapping("/summary/{symbol}")
    public ResponseEntity<ApiResponse<IndexSummaryDto>> getLatestIndexSummary(@PathVariable String symbol) {
        return marketService.getLatestIndexSummaryBySymbol(symbol)
                .map(marketMapper::toIndexSummaryDto)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Index summary retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary/{symbol}/history")
    public ResponseEntity<ApiResponse<List<IndexSummaryDto>>> getIndexSummaryHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<MarketIndexSummary> summaries = marketService.getIndexSummariesByDateRange(symbol, startDate, endDate);
        List<IndexSummaryDto> dtos = marketMapper.toIndexSummaryDtoList(summaries);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Index summary history retrieved successfully"));
    }
}

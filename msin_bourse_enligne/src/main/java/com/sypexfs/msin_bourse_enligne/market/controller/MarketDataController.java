package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.dto.*;
import com.sypexfs.msin_bourse_enligne.market.entity.*;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketService marketService;
    private final MarketMapper marketMapper;
    private final MarketWebSocketHandler webSocketHandler;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<MarketOverviewDto>> getMarketOverview() {
        List<MarketIndexSummary> indices = marketService.getIndexOverview();
        List<MarketSummary> topGainers = marketService.getTopGainers(10);
        List<MarketSummary> topLosers = marketService.getTopLosers(10);
        List<MarketSummary> mostActive = marketService.getMostActive(10);

        MarketOverviewDto overview = MarketOverviewDto.builder()
                .indices(marketMapper.toIndexSummaryDtoList(indices))
                .topGainers(marketMapper.toMarketSummaryDtoList(topGainers))
                .topLosers(marketMapper.toMarketSummaryDtoList(topLosers))
                .mostActive(marketMapper.toMarketSummaryDtoList(mostActive))
                .build();

        return ResponseEntity.ok(ApiResponse.success(overview, "Market overview retrieved successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<MarketSummaryDto>>> getAllSummaries() {
        List<MarketSummary> summaries = marketService.getAllSummaries();
        List<MarketSummaryDto> dtos = marketMapper.toMarketSummaryDtoList(summaries);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Market summaries retrieved successfully"));
    }

    @GetMapping("/summary/{symbol}")
    public ResponseEntity<ApiResponse<MarketSummaryDto>> getLatestSummaryBySymbol(@PathVariable String symbol) {
        return marketService.getLatestSummaryBySymbol(symbol)
                .map(marketMapper::toMarketSummaryDto)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Market summary retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary/{symbol}/history")
    public ResponseEntity<ApiResponse<List<MarketSummaryDto>>> getSummaryHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<MarketSummary> summaries = marketService.getSummariesByDateRange(symbol, startDate, endDate);
        List<MarketSummaryDto> dtos = marketMapper.toMarketSummaryDtoList(summaries);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Summary history retrieved successfully"));
    }

    @GetMapping("/gainers")
    public ResponseEntity<ApiResponse<List<MarketSummaryDto>>> getTopGainers(
            @RequestParam(defaultValue = "10") int limit) {
        List<MarketSummary> gainers = marketService.getTopGainers(limit);
        List<MarketSummaryDto> dtos = marketMapper.toMarketSummaryDtoList(gainers);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Top gainers retrieved successfully"));
    }

    @GetMapping("/losers")
    public ResponseEntity<ApiResponse<List<MarketSummaryDto>>> getTopLosers(
            @RequestParam(defaultValue = "10") int limit) {
        List<MarketSummary> losers = marketService.getTopLosers(limit);
        List<MarketSummaryDto> dtos = marketMapper.toMarketSummaryDtoList(losers);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Top losers retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MarketSummaryDto>>> getMostActive(
            @RequestParam(defaultValue = "10") int limit) {
        List<MarketSummary> active = marketService.getMostActive(limit);
        List<MarketSummaryDto> dtos = marketMapper.toMarketSummaryDtoList(active);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Most active stocks retrieved successfully"));
    }

    @GetMapping("/stock/{symbol}")
    public ResponseEntity<ApiResponse<StockDetailDto>> getStockDetail(@PathVariable String symbol) {
        MarketInstrument instrument = marketService.getInstrumentBySymbol(symbol).orElse(null);
        MarketSummary summary = marketService.getLatestSummaryBySymbol(symbol).orElse(null);
        List<MarketOrderbook> buyOrders = marketService.getBuyOrdersBySymbol(symbol);
        List<MarketOrderbook> sellOrders = marketService.getSellOrdersBySymbol(symbol);
        List<MarketTransaction> transactions = marketService.getTransactionsBySymbol(symbol, 50);

        StockDetailDto detail = StockDetailDto.builder()
                .instrument(marketMapper.toInstrumentDto(instrument))
                .summary(marketMapper.toMarketSummaryDto(summary))
                .buyOrders(marketMapper.toOrderbookDtoList(buyOrders))
                .sellOrders(marketMapper.toOrderbookDtoList(sellOrders))
                .recentTransactions(marketMapper.toTransactionDtoList(transactions))
                .build();

        return ResponseEntity.ok(ApiResponse.success(detail, "Stock detail retrieved successfully"));
    }

    // Test endpoint to trigger WebSocket broadcast
    @PostMapping("/test/broadcast/{symbol}")
    public ResponseEntity<ApiResponse<String>> testBroadcast(@PathVariable String symbol) {
        return marketService.getLatestSummaryBySymbol(symbol)
                .map(summary -> {
                    MarketSummaryDto dto = marketMapper.toMarketSummaryDto(summary);
                    webSocketHandler.broadcastMarketSummary(dto);
                    return ResponseEntity.ok(ApiResponse.success(
                        "Broadcast sent to market.summary channel",
                        "WebSocket broadcast triggered successfully"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Test endpoint to update summary variation and broadcast
    @PostMapping("/test/update-variation/{symbol}")
    public ResponseEntity<ApiResponse<MarketSummaryDto>> testUpdateVariation(
            @PathVariable String symbol,
            @RequestParam BigDecimal variation) {
        return marketService.getLatestSummaryBySymbol(symbol)
                .map(summary -> {
                    // Update the variation
                    summary.setVariation(variation);
                    
                    // Calculate new price based on variation
                    if (summary.getLastClosingPrice() != null) {
                        BigDecimal variationValue = variation
                            .divide(BigDecimal.valueOf(100)); // divide variation by 100
                        BigDecimal factor = BigDecimal.ONE.add(variationValue); // (1 + variation / 100)
                        BigDecimal newPrice = summary.getLastClosingPrice()
                            .multiply(factor); // multiply by (1 + variation / 100)
                        summary.setPrice(newPrice);
                    }

                    
                    // Save updated summary
                    MarketSummary updatedSummary = marketService.saveSummary(summary);
                    
                    // Broadcast to WebSocket
                    MarketSummaryDto dto = marketMapper.toMarketSummaryDto(updatedSummary);
                    webSocketHandler.broadcastMarketSummary(dto);
                    
                    // Also broadcast market overview update
                    List<MarketIndexSummary> indices = marketService.getIndexOverview();
                    List<MarketSummary> topGainers = marketService.getTopGainers(10);
                    List<MarketSummary> topLosers = marketService.getTopLosers(10);
                    List<MarketSummary> mostActive = marketService.getMostActive(10);
                    
                    MarketOverviewDto overview = MarketOverviewDto.builder()
                            .indices(marketMapper.toIndexSummaryDtoList(indices))
                            .topGainers(marketMapper.toMarketSummaryDtoList(topGainers))
                            .topLosers(marketMapper.toMarketSummaryDtoList(topLosers))
                            .mostActive(marketMapper.toMarketSummaryDtoList(mostActive))
                            .build();
                    
                    webSocketHandler.broadcast("market.overview", "data", overview);
                    
                    return ResponseEntity.ok(ApiResponse.success(
                        dto,
                        "Summary updated and broadcast successfully"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

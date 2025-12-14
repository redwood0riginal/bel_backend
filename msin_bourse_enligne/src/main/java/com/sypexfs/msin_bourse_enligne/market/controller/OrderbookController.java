package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.dto.OrderbookDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketOrderbook;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/market/orderbook")
@RequiredArgsConstructor
public class OrderbookController {

    private final MarketService marketService;
    private final MarketMapper marketMapper;

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<List<OrderbookDto>>> getOrderbookBySymbol(@PathVariable String symbol) {
        List<MarketOrderbook> orderbook = marketService.getActiveOrderbookBySymbol(symbol);
        List<OrderbookDto> dtos = marketMapper.toOrderbookDtoList(orderbook);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Orderbook retrieved successfully"));
    }

    @GetMapping("/{symbol}/buy")
    public ResponseEntity<ApiResponse<List<OrderbookDto>>> getBuyOrders(@PathVariable String symbol) {
        List<MarketOrderbook> orders = marketService.getBuyOrdersBySymbol(symbol);
        List<OrderbookDto> dtos = marketMapper.toOrderbookDtoList(orders);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Buy orders retrieved successfully"));
    }

    @GetMapping("/{symbol}/sell")
    public ResponseEntity<ApiResponse<List<OrderbookDto>>> getSellOrders(@PathVariable String symbol) {
        List<MarketOrderbook> orders = marketService.getSellOrdersBySymbol(symbol);
        List<OrderbookDto> dtos = marketMapper.toOrderbookDtoList(orders);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Sell orders retrieved successfully"));
    }

    @GetMapping("/order/{orderMarketId}")
    public ResponseEntity<ApiResponse<OrderbookDto>> getOrderByMarketId(@PathVariable String orderMarketId) {
        return marketService.getOrderByMarketId(orderMarketId)
                .map(marketMapper::toOrderbookDto)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Order retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }
}

package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.dto.TransactionDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketTransaction;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/market/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final MarketService marketService;
    private final MarketMapper marketMapper;

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getRecentTransactions(
            @RequestParam(defaultValue = "1000") int limit) {
        List<MarketTransaction> transactions = marketService.getRecentTransactions(limit);
        List<TransactionDto> dtos = marketMapper.toTransactionDtoList(transactions);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Recent transactions retrieved successfully"));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsBySymbol(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "50") int limit) {
        List<MarketTransaction> transactions = marketService.getTransactionsBySymbol(symbol, limit);
        List<TransactionDto> dtos = marketMapper.toTransactionDtoList(transactions);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Transactions retrieved successfully"));
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<MarketTransaction> transactions = marketService.getTransactionsByDateRange(symbol, startDate, endDate);
        List<TransactionDto> dtos = marketMapper.toTransactionDtoList(transactions);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Transaction history retrieved successfully"));
    }

    @GetMapping("/exec/{execId}")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransactionByExecId(@PathVariable String execId) {
        return marketService.getTransactionByExecId(execId)
                .map(marketMapper::toTransactionDto)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Transaction retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }
}

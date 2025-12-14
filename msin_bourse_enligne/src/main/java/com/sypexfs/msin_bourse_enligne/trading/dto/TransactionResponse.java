package com.sypexfs.msin_bourse_enligne.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private String symbol;
    private String side; // 'BUY' or 'SELL'
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal commission;
    private BigDecimal tax;
    private BigDecimal netAmount;
    private LocalDateTime transactionDate;
    private LocalDate settlementDate;
    private String status; // 'PENDING', 'SETTLED', 'FAILED'
    private LocalDateTime createdAt;
}

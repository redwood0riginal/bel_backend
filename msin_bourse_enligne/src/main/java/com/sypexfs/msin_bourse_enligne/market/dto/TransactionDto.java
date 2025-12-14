package com.sypexfs.msin_bourse_enligne.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private Long id;
    private String symbol;
    private String execId;
    private String execType;
    private String side; // 'BUY' or 'SELL'
    private LocalDateTime dateTrans;
    private BigDecimal quantity;
    private BigDecimal price;
    private String sequence;
    private String tradeType;
    private String orderId;
}

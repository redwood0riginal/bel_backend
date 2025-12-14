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
public class OrderbookDto {

    private Long id;
    private String symbol;
    private String side; // 'BUY' or 'SELL'
    private BigDecimal quantity;
    private BigDecimal price;
    private Integer orderCount;
    private LocalDateTime dateOrder;
    private String orderMarketId;
    private String orderType;
    private Boolean isOwnOrder;
}

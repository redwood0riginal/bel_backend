package com.sypexfs.msin_bourse_enligne.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatistics {

    private Long totalOrders;
    private Long pendingOrders;
    private Long filledOrders;
    private Long cancelledOrders;
    private Long partialOrders;
    private Long rejectedOrders;
    
    private Long totalBuyOrders;
    private Long totalSellOrders;
    
    private BigDecimal totalBuyAmount;
    private BigDecimal totalSellAmount;
    private BigDecimal totalCommission;
    private BigDecimal totalTax;
    
    private BigDecimal averageOrderSize;
    private BigDecimal averageExecutionPrice;
}

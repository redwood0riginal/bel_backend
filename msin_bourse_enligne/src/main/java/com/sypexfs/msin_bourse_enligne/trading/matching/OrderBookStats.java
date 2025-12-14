package com.sypexfs.msin_bourse_enligne.trading.matching;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Order book statistics
 */
@Data
@AllArgsConstructor
public class OrderBookStats {
    private String symbol;
    private int totalBuyOrders;
    private int totalSellOrders;
    private BigDecimal totalBuyVolume;
    private BigDecimal totalSellVolume;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal spread;
    private BigDecimal midPrice;
}

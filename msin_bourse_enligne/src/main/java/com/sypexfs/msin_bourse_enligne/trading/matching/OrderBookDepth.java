package com.sypexfs.msin_bourse_enligne.trading.matching;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Order book depth showing top N price levels
 */
@Data
@AllArgsConstructor
public class OrderBookDepth {
    private String symbol;
    private List<PriceLevel> bids;
    private List<PriceLevel> asks;
}

package com.sypexfs.msin_bourse_enligne.trading.matching;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Price level in order book
 */
@Data
@AllArgsConstructor
public class PriceLevel {
    private BigDecimal price;
    private BigDecimal volume;
}

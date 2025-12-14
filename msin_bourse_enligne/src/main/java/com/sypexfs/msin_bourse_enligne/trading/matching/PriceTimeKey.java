package com.sypexfs.msin_bourse_enligne.trading.matching;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Key for ordering orders by price-time priority
 */
@Data
@AllArgsConstructor
public class PriceTimeKey {
    private BigDecimal price;
    private LocalDateTime timestamp;
    private Long orderId; // For uniqueness
}

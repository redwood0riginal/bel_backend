package com.sypexfs.msin_bourse_enligne.trading.matching;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single execution between two orders
 */
@Data
@AllArgsConstructor
public class OrderExecution {
    private Long aggressorOrderId;
    private Long passiveOrderId;
    private BigDecimal quantity;
    private BigDecimal price;
    private LocalDateTime timestamp;
    
    public BigDecimal getAmount() {
        return quantity.multiply(price);
    }
}

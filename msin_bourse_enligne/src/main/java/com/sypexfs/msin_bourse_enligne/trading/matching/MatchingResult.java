package com.sypexfs.msin_bourse_enligne.trading.matching;

import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of order matching process
 */
@Data
@AllArgsConstructor
public class MatchingResult {
    private Order order;
    private List<OrderExecution> executions;
    private String status; // FILLED, PARTIAL, PENDING, REJECTED
    private String message;
    
    public MatchingResult(Order order, List<OrderExecution> executions) {
        this.order = order;
        this.executions = executions;
        this.status = order.getStatId();
        this.message = null;
    }
    
    public static MatchingResult rejected(Order order, String reason) {
        return new MatchingResult(order, new ArrayList<>(), "REJECTED", reason);
    }
    
    public static MatchingResult pending(Order order) {
        return new MatchingResult(order, new ArrayList<>(), "PENDING", "Order pending in book");
    }
    
    public boolean isExecuted() {
        return !executions.isEmpty();
    }
    
    public boolean isFilled() {
        return "FILLED".equals(status);
    }
    
    public boolean isPartial() {
        return "PARTIAL".equals(status);
    }
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
}

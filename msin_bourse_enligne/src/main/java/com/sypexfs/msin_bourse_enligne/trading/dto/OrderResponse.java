package com.sypexfs.msin_bourse_enligne.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private Integer sign;
    private BigDecimal price;
    private BigDecimal stopPrice;
    private BigDecimal displayedQuantity;
    private BigDecimal quantity;
    private BigDecimal orderAmount;
    private LocalDate dateOrder;
    private LocalDate dateExpiry;
    private LocalTime timeOrder;
    private LocalTime timeExpiry;
    private LocalDateTime dateEntry;
    private BigDecimal execQty;
    private BigDecimal execAvgPrice;
    private String expiryTypeId;
    private String statId;
    private String orderTypeId;
    private Long brokerId;
    private Long cashAccountId;
    private Long entityId;
    private Long portfId;
    private Long subPortfId;
    private Long secAccountId;
    private Long secId;
    private String symbol;
    private String subRedTypeId;
    private String externalRef;
    private String classId;
    private String accountType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private BigDecimal remainingQuantity;
    private String orderSide; // "BUY" or "SELL"
}

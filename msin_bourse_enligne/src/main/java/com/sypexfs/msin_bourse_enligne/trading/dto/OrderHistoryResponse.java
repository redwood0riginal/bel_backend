package com.sypexfs.msin_bourse_enligne.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryResponse {

    private Long id;
    private Long orderId;
    private String previousStatus;
    private String newStatus;
    private BigDecimal previousExecQty;
    private BigDecimal newExecQty;
    private String changeReason;
    private Long changedBy;
    private LocalDateTime changedAt;
}

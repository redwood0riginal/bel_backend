package com.sypexfs.msin_bourse_enligne.portfolio.dto;

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
public class PositionResponse {
    private Long id;
    private Long portfolioId;
    private String symbol;
    private String libelle;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal performancePercent;
    private BigDecimal weightPercent;
    private LocalDateTime lastUpdated;
}

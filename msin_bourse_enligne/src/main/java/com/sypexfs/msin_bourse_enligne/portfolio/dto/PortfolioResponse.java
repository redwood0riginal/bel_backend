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
public class PortfolioResponse {
    private Long id;
    private Long userId;
    private String accountNumber;
    private String accountType;
    private String currency;
    private String status;
    private BigDecimal totalValue;          // Valeur totale portefeuille
    private BigDecimal securitiesValue;     // Valeur totale titres
    private BigDecimal cashBalance;         // Solde en espèces
    private BigDecimal investedAmount;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercent;
    private BigDecimal dayPnl;
    private BigDecimal dayPnlPercent;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
}

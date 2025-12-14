package com.sypexfs.msin_bourse_enligne.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentDto {

    private Long id;
    private String marketPlace;
    private String symbol;
    private String name;
    private String classId;
    private LocalDate issueDate;
    private LocalDate maturityDate;
    private LocalDateTime lastTradeTime;
    private String issuer;
    private String marketSegment;
    private String priceType;
    private String matchType;
    private String tradingType;
    private String tradingStatus;
    private String currency;
    private String haltReason;
    private BigDecimal highPx;
    private BigDecimal lowPx;
    private BigDecimal lastPx;
    private BigDecimal cumQty;
    private String sector;
    private String marketType;
    private BigDecimal issuedQty;
}

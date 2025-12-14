package com.sypexfs.msin_bourse_enligne.market.dto;

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
public class MarketSummaryDto {

    private Long id;
    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal variation;
    private BigDecimal lastClosingPrice;
    private BigDecimal closingPrice;
    private BigDecimal openingPrice;
    private BigDecimal higherPrice;
    private BigDecimal lowerPrice;
    private BigDecimal vwap;
    private BigDecimal quantity;
    private BigDecimal volume;
    private BigDecimal top;
    private BigDecimal tov;
    private LocalDateTime dateTrans;
    private LocalDateTime dateUpdate;
    private String displayName;
    private Boolean isPositive;
}

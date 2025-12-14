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
public class IndexSummaryDto {

    private Long id;
    private String symbol;
    private BigDecimal price;
    private BigDecimal closingPrice;
    private BigDecimal openingPrice;
    private BigDecimal lastClosingPrice;
    private BigDecimal lowerPrice;
    private BigDecimal higherPrice;
    private BigDecimal variation;
    private LocalDateTime datePrice;
    private Boolean isPositive;
}

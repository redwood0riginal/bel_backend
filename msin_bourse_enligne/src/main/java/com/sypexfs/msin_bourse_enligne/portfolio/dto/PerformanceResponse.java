package com.sypexfs.msin_bourse_enligne.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResponse {
    private BigDecimal totalReturn;
    private BigDecimal totalReturnPercent;
    private BigDecimal dayReturn;
    private BigDecimal dayReturnPercent;
    private BigDecimal weekReturn;
    private BigDecimal weekReturnPercent;
    private BigDecimal monthReturn;
    private BigDecimal monthReturnPercent;
    private BigDecimal yearReturn;
    private BigDecimal yearReturnPercent;
    private List<PerformanceDataPoint> historicalData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceDataPoint {
        private LocalDate date;
        private BigDecimal value;
        private BigDecimal returnPercent;
    }
}

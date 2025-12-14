package com.sypexfs.msin_bourse_enligne.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortfolioRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Account type is required")
    private String accountType; // 'CASH', 'MARGIN', 'RETIREMENT'
    
    @Builder.Default
    private String currency = "MAD";
    
    @Builder.Default
    private BigDecimal initialCashBalance = BigDecimal.ZERO;
}

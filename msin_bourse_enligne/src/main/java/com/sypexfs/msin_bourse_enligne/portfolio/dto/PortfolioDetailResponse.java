package com.sypexfs.msin_bourse_enligne.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDetailResponse {
    private PortfolioResponse portfolio;
    private List<PositionResponse> positions;
}

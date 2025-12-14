package com.sypexfs.msin_bourse_enligne.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOverviewDto {

    private List<IndexSummaryDto> indices;
    private List<MarketSummaryDto> topGainers;
    private List<MarketSummaryDto> topLosers;
    private List<MarketSummaryDto> mostActive;
}

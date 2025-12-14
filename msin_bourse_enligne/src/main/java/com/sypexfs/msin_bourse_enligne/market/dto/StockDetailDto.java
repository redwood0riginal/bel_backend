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
public class StockDetailDto {

    private InstrumentDto instrument;
    private MarketSummaryDto summary;
    private List<OrderbookDto> buyOrders;
    private List<OrderbookDto> sellOrders;
    private List<TransactionDto> recentTransactions;
}

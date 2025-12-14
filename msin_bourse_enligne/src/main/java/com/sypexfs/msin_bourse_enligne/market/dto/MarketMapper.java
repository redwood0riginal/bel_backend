package com.sypexfs.msin_bourse_enligne.market.dto;

import com.sypexfs.msin_bourse_enligne.market.entity.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MarketMapper {

    public InstrumentDto toInstrumentDto(MarketInstrument entity) {
        if (entity == null) return null;
        return InstrumentDto.builder()
                .id(entity.getId())
                .marketPlace(entity.getMarketPlace())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .classId(entity.getClassId())
                .issueDate(entity.getIssueDate())
                .maturityDate(entity.getMaturityDate())
                .lastTradeTime(entity.getLastTradeTime())
                .issuer(entity.getIssuer())
                .marketSegment(entity.getMarketSegment())
                .priceType(entity.getPriceType())
                .matchType(entity.getMatchType())
                .tradingType(entity.getTradingType())
                .tradingStatus(entity.getTradingStatus())
                .currency(entity.getCurrency())
                .haltReason(entity.getHaltReason())
                .highPx(entity.getHighPx())
                .lowPx(entity.getLowPx())
                .lastPx(entity.getLastPx())
                .cumQty(entity.getCumQty())
                .sector(entity.getSector())
                .marketType(entity.getMarketType())
                .issuedQty(entity.getIssuedQty())
                .build();
    }

    public MarketSummaryDto toMarketSummaryDto(MarketSummary entity) {
        if (entity == null) return null;
        return MarketSummaryDto.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .price(entity.getPrice())
                .variation(entity.getVariation())
                .lastClosingPrice(entity.getLastClosingPrice())
                .closingPrice(entity.getClosingPrice())
                .openingPrice(entity.getOpeningPrice())
                .higherPrice(entity.getHigherPrice())
                .lowerPrice(entity.getLowerPrice())
                .vwap(entity.getVwap())
                .quantity(entity.getQuantity())
                .volume(entity.getVolume())
                .top(entity.getTop())
                .tov(entity.getTov())
                .dateTrans(entity.getDateTrans())
                .dateUpdate(entity.getDateUpdate())
                .displayName(entity.getDisplayName())
                .isPositive(entity.isPositive())
                .build();
    }

    public IndexDto toIndexDto(MarketIndex entity) {
        if (entity == null) return null;
        return IndexDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .marketPlace(entity.getMarketPlace())
                .indexType(entity.getIndexType())
                .price(entity.getPrice())
                .datePrice(entity.getDatePrice())
                .build();
    }

    public IndexSummaryDto toIndexSummaryDto(MarketIndexSummary entity) {
        if (entity == null) return null;
        return IndexSummaryDto.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .price(entity.getPrice())
                .closingPrice(entity.getClosingPrice())
                .openingPrice(entity.getOpeningPrice())
                .lastClosingPrice(entity.getLastClosingPrice())
                .lowerPrice(entity.getLowerPrice())
                .higherPrice(entity.getHigherPrice())
                .variation(entity.getVariation())
                .datePrice(entity.getDatePrice())
                .isPositive(entity.isPositive())
                .build();
    }

    public OrderbookDto toOrderbookDto(MarketOrderbook entity) {
        if (entity == null) return null;
        return OrderbookDto.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .side(entity.getSide())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .orderCount(entity.getOrderCount())
                .dateOrder(entity.getDateOrder())
                .orderMarketId(entity.getOrderMarketId())
                .orderType(entity.getOrderType())
                .isOwnOrder(entity.getIsOwnOrder())
                .build();
    }

    public TransactionDto toTransactionDto(MarketTransaction entity) {
        if (entity == null) return null;
        return TransactionDto.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .execId(entity.getExecId())
                .execType(entity.getExecType())
                .side(entity.getSide())
                .dateTrans(entity.getDateTrans())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .sequence(entity.getSequence())
                .tradeType(entity.getTradeType())
                .orderId(entity.getOrderId())
                .build();
    }

    public NewsDto toNewsDto(MarketNews entity) {
        if (entity == null) return null;
        return NewsDto.builder()
                .id(entity.getId())
                .headline(entity.getHeadline())
                .urgency(entity.getUrgency())
                .linesOfText(entity.getLinesOfText())
                .urlLink(entity.getUrlLink())
                .publishedAt(entity.getPublishedAt())
                .build();
    }

    // List mappers
    public List<InstrumentDto> toInstrumentDtoList(List<MarketInstrument> entities) {
        return entities.stream().map(this::toInstrumentDto).collect(Collectors.toList());
    }

    public List<MarketSummaryDto> toMarketSummaryDtoList(List<MarketSummary> entities) {
        return entities.stream().map(this::toMarketSummaryDto).collect(Collectors.toList());
    }

    public List<IndexDto> toIndexDtoList(List<MarketIndex> entities) {
        return entities.stream().map(this::toIndexDto).collect(Collectors.toList());
    }

    public List<IndexSummaryDto> toIndexSummaryDtoList(List<MarketIndexSummary> entities) {
        return entities.stream().map(this::toIndexSummaryDto).collect(Collectors.toList());
    }

    public List<OrderbookDto> toOrderbookDtoList(List<MarketOrderbook> entities) {
        return entities.stream().map(this::toOrderbookDto).collect(Collectors.toList());
    }

    public List<TransactionDto> toTransactionDtoList(List<MarketTransaction> entities) {
        return entities.stream().map(this::toTransactionDto).collect(Collectors.toList());
    }

    public List<NewsDto> toNewsDtoList(List<MarketNews> entities) {
        return entities.stream().map(this::toNewsDto).collect(Collectors.toList());
    }

    public MarketIndexResponseDto toMarketIndexResponseDto(MarketIndex entity) {
        if (entity == null) return null;
        return MarketIndexResponseDto.builder()
                .objectType("MarketIndex")
                .id(entity.getId())
                .indexId(entity.getCode())
                .name(entity.getName())
                .indexType(entity.getIndexType())
                .price(entity.getPrice())
                .datePrice(entity.getDatePrice() != null ? 
                    Timestamp.valueOf(entity.getDatePrice()) : null)
                .build();
    }

    public List<MarketIndexResponseDto> toMarketIndexResponseDtoList(List<MarketIndex> entities) {
        return entities.stream().map(this::toMarketIndexResponseDto).collect(Collectors.toList());
    }
}

package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketIndexSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketIndexSummaryRepository extends JpaRepository<MarketIndexSummary, Long> {

    Optional<MarketIndexSummary> findBySymbol(String symbol);

    @Query("SELECT s FROM MarketIndexSummary s WHERE s.symbol = :symbol ORDER BY s.datePrice DESC")
    List<MarketIndexSummary> findBySymbolOrderByDatePriceDesc(@Param("symbol") String symbol);

    @Query("SELECT s FROM MarketIndexSummary s WHERE s.symbol = :symbol ORDER BY s.datePrice DESC LIMIT 1")
    Optional<MarketIndexSummary> findLatestBySymbol(@Param("symbol") String symbol);

    @Query("SELECT s FROM MarketIndexSummary s WHERE s.symbol = :symbol AND s.datePrice BETWEEN :startDate AND :endDate ORDER BY s.datePrice")
    List<MarketIndexSummary> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM MarketIndexSummary s ORDER BY s.datePrice DESC")
    List<MarketIndexSummary> findAllOrderByDatePriceDesc();
    
    Optional<MarketIndexSummary> findTopBySymbolOrderByDatePriceDesc(String symbol);
}

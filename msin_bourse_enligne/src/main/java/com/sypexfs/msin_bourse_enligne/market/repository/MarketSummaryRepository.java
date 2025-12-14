package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketSummaryRepository extends JpaRepository<MarketSummary, Long> {

    Optional<MarketSummary> findBySymbol(String symbol);

    List<MarketSummary> findBySymbolOrderByDateTransDesc(String symbol);

    @Query("SELECT s FROM MarketSummary s WHERE s.symbol = :symbol ORDER BY s.dateTrans DESC")
    Optional<MarketSummary> findLatestBySymbol(@Param("symbol") String symbol);

    @Query("SELECT s FROM MarketSummary s ORDER BY s.variation DESC")
    List<MarketSummary> findTopGainers(Pageable pageable);

    @Query("SELECT s FROM MarketSummary s ORDER BY s.variation ASC")
    List<MarketSummary> findTopLosers(Pageable pageable);

    @Query("SELECT s FROM MarketSummary s ORDER BY s.volume DESC")
    List<MarketSummary> findMostActive(Pageable pageable);

    @Query("SELECT s FROM MarketSummary s WHERE s.dateTrans >= :startDate ORDER BY s.dateTrans DESC")
    List<MarketSummary> findRecentSummaries(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT s FROM MarketSummary s WHERE s.symbol = :symbol AND s.dateTrans BETWEEN :startDate AND :endDate ORDER BY s.dateTrans")
    List<MarketSummary> findBySymbolAndDateRange(@Param("symbol") String symbol, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}

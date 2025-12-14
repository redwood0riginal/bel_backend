package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketOrderbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketOrderbookRepository extends JpaRepository<MarketOrderbook, Long> {

    List<MarketOrderbook> findBySymbol(String symbol);

    List<MarketOrderbook> findBySymbolAndSide(String symbol, String side);

    @Query("SELECT o FROM MarketOrderbook o WHERE o.symbol = :symbol AND o.side = :side AND o.delete = false ORDER BY o.price DESC")
    List<MarketOrderbook> findActiveOrdersBySymbolAndSide(@Param("symbol") String symbol, @Param("side") String side);

    @Query("SELECT o FROM MarketOrderbook o WHERE o.symbol = :symbol AND o.delete = false ORDER BY CASE WHEN o.side = 'BUY' THEN o.price END DESC, CASE WHEN o.side = 'SELL' THEN o.price END ASC")
    List<MarketOrderbook> findActiveOrderbookBySymbol(@Param("symbol") String symbol);

    Optional<MarketOrderbook> findByOrderMarketId(String orderMarketId);

    @Query("SELECT o FROM MarketOrderbook o WHERE o.symbol = :symbol AND o.isOwnOrder = true AND o.delete = false")
    List<MarketOrderbook> findOwnOrdersBySymbol(@Param("symbol") String symbol);
}

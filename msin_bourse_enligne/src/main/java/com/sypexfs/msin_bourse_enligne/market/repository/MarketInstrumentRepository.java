package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketInstrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketInstrumentRepository extends JpaRepository<MarketInstrument, Long> {

    Optional<MarketInstrument> findBySymbol(String symbol);

    List<MarketInstrument> findBySector(String sector);

    List<MarketInstrument> findByMarketSegment(String marketSegment);

    List<MarketInstrument> findByTradingStatus(String tradingStatus);

    @Query("SELECT DISTINCT i.sector FROM MarketInstrument i WHERE i.sector IS NOT NULL ORDER BY i.sector")
    List<String> findAllDistinctSectors();

    @Query("SELECT i FROM MarketInstrument i WHERE i.tradingStatus = 'ACTIVE' ORDER BY i.symbol")
    List<MarketInstrument> findAllActiveInstruments();

    @Query("SELECT i FROM MarketInstrument i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.symbol) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MarketInstrument> searchByKeyword(@Param("keyword") String keyword);
}

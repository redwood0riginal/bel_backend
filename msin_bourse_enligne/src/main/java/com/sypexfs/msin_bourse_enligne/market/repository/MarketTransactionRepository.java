package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketTransactionRepository extends JpaRepository<MarketTransaction, Long> {

    Optional<MarketTransaction> findByExecId(String execId);

    List<MarketTransaction> findBySymbol(String symbol);

    @Query("SELECT t FROM MarketTransaction t WHERE t.symbol = :symbol AND t.cancel = false ORDER BY t.dateTrans DESC")
    List<MarketTransaction> findActiveTransactionsBySymbol(@Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT t FROM MarketTransaction t WHERE t.symbol = :symbol AND t.dateTrans BETWEEN :startDate AND :endDate AND t.cancel = false ORDER BY t.dateTrans DESC")
    List<MarketTransaction> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM MarketTransaction t WHERE t.cancel = false ORDER BY t.dateTrans DESC")
    List<MarketTransaction> findRecentTransactions(Pageable pageable);

    @Query("SELECT t FROM MarketTransaction t WHERE t.dateTrans >= :startDate AND t.cancel = false ORDER BY t.dateTrans DESC")
    List<MarketTransaction> findTransactionsSince(@Param("startDate") LocalDateTime startDate);
}

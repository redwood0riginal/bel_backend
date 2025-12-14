package com.sypexfs.msin_bourse_enligne.trading.repository;

import com.sypexfs.msin_bourse_enligne.trading.entity.UserTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserTransactionRepository extends JpaRepository<UserTransaction, Long> {

    // Find transactions by user
    List<UserTransaction> findByUserId(Long userId);

    // Find transactions by user with pagination
    List<UserTransaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    // Find transactions by order
    List<UserTransaction> findByOrderId(Long orderId);

    // Find transactions by symbol
    List<UserTransaction> findBySymbol(String symbol);

    // Find transactions by user and symbol
    List<UserTransaction> findByUserIdAndSymbol(Long userId, String symbol);

    // Find buy transactions by user
    @Query("SELECT t FROM UserTransaction t WHERE t.userId = :userId AND t.side = 'BUY' ORDER BY t.transactionDate DESC")
    List<UserTransaction> findBuyTransactionsByUser(@Param("userId") Long userId, Pageable pageable);

    // Find sell transactions by user
    @Query("SELECT t FROM UserTransaction t WHERE t.userId = :userId AND t.side = 'SELL' ORDER BY t.transactionDate DESC")
    List<UserTransaction> findSellTransactionsByUser(@Param("userId") Long userId, Pageable pageable);

    // Find transactions by date range
    @Query("SELECT t FROM UserTransaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<UserTransaction> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);

    // Find transactions by symbol and date range
    @Query("SELECT t FROM UserTransaction t WHERE t.symbol = :symbol AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<UserTransaction> findBySymbolAndDateRange(@Param("symbol") String symbol, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);

    // Find recent transactions
    @Query("SELECT t FROM UserTransaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC")
    List<UserTransaction> findRecentTransactionsByUser(@Param("userId") Long userId, Pageable pageable);

    // Find pending transactions
    @Query("SELECT t FROM UserTransaction t WHERE t.userId = :userId AND t.status = 'PENDING' ORDER BY t.transactionDate DESC")
    List<UserTransaction> findPendingTransactionsByUser(@Param("userId") Long userId);

    // Find settled transactions
    @Query("SELECT t FROM UserTransaction t WHERE t.userId = :userId AND t.status = 'SETTLED' ORDER BY t.transactionDate DESC")
    List<UserTransaction> findSettledTransactionsByUser(@Param("userId") Long userId, Pageable pageable);

    // Calculate total buy amount for user and symbol
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM UserTransaction t WHERE t.userId = :userId AND t.symbol = :symbol AND t.side = 'BUY' AND t.status = 'SETTLED'")
    Double calculateTotalBuyAmount(@Param("userId") Long userId, @Param("symbol") String symbol);

    // Calculate total sell amount for user and symbol
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM UserTransaction t WHERE t.userId = :userId AND t.symbol = :symbol AND t.side = 'SELL' AND t.status = 'SETTLED'")
    Double calculateTotalSellAmount(@Param("userId") Long userId, @Param("symbol") String symbol);

    // Get transaction statistics for user
    @Query("SELECT t.side, COUNT(t), SUM(t.amount) FROM UserTransaction t WHERE t.userId = :userId AND t.status = 'SETTLED' GROUP BY t.side")
    List<Object[]> getTransactionStatsByUser(@Param("userId") Long userId);
}

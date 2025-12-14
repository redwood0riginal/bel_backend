package com.sypexfs.msin_bourse_enligne.trading.repository;

import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by user
    List<Order> findByUserId(Long userId);

    // Find orders by user with pagination
    List<Order> findByUserIdOrderByDateEntryDesc(Long userId, Pageable pageable);

    // Find orders by user and status
    List<Order> findByUserIdAndStatId(Long userId, String statId);

    // Find orders by symbol
    List<Order> findBySymbol(String symbol);

    // Find orders by user and symbol
    List<Order> findByUserIdAndSymbol(Long userId, String symbol);

    // Find pending orders by user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.statId = 'PENDING' ORDER BY o.dateEntry DESC")
    List<Order> findPendingOrdersByUser(@Param("userId") Long userId);

    // Find active orders (PENDING or PARTIAL)
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.statId IN ('PENDING', 'PARTIAL') ORDER BY o.dateEntry DESC")
    List<Order> findActiveOrdersByUser(@Param("userId") Long userId);

    // Find filled orders by user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.statId = 'FILLED' ORDER BY o.dateEntry DESC")
    List<Order> findFilledOrdersByUser(@Param("userId") Long userId, Pageable pageable);

    // Find cancelled orders by user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.statId = 'CANCELLED' ORDER BY o.dateEntry DESC")
    List<Order> findCancelledOrdersByUser(@Param("userId") Long userId, Pageable pageable);

    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.dateEntry BETWEEN :startDate AND :endDate ORDER BY o.dateEntry DESC")
    List<Order> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Find orders by symbol and date range
    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.dateEntry BETWEEN :startDate AND :endDate ORDER BY o.dateEntry DESC")
    List<Order> findBySymbolAndDateRange(@Param("symbol") String symbol, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Find buy orders by user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.sign = 1 ORDER BY o.dateEntry DESC")
    List<Order> findBuyOrdersByUser(@Param("userId") Long userId, Pageable pageable);

    // Find sell orders by user
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.sign = -1 ORDER BY o.dateEntry DESC")
    List<Order> findSellOrdersByUser(@Param("userId") Long userId, Pageable pageable);

    // Count pending orders by user
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.statId IN ('PENDING', 'PARTIAL')")
    Long countActiveOrdersByUser(@Param("userId") Long userId);

    // Find order by external reference
    Optional<Order> findByExternalRef(String externalRef);

    // Find orders by portfolio
    List<Order> findByPortfId(Long portfId);

    // Find recent orders
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.dateEntry DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") Long userId, Pageable pageable);

    // Check if user has pending orders for symbol
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o " +
           "WHERE o.userId = :userId AND o.symbol = :symbol AND o.statId IN ('PENDING', 'PARTIAL')")
    boolean hasPendingOrdersForSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);
}

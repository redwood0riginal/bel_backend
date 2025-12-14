package com.sypexfs.msin_bourse_enligne.trading.repository;

import com.sypexfs.msin_bourse_enligne.trading.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    // Find history by order
    List<OrderHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);

    // Find history by order and date range
    @Query("SELECT h FROM OrderHistory h WHERE h.order.id = :orderId AND h.changedAt BETWEEN :startDate AND :endDate ORDER BY h.changedAt DESC")
    List<OrderHistory> findByOrderIdAndDateRange(@Param("orderId") Long orderId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    // Find recent history entries for an order
    @Query("SELECT h FROM OrderHistory h WHERE h.order.id = :orderId ORDER BY h.changedAt DESC")
    List<OrderHistory> findRecentHistoryByOrder(@Param("orderId") Long orderId);

    // Find history by status change
    @Query("SELECT h FROM OrderHistory h WHERE h.order.id = :orderId AND h.newStatus = :status ORDER BY h.changedAt DESC")
    List<OrderHistory> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") String status);
}

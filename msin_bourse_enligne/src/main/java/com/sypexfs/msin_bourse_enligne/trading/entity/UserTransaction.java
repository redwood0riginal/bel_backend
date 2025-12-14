package com.sypexfs.msin_bourse_enligne.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_transactions", schema = "trading_schema",
        indexes = {
                @Index(name = "idx_user_trans_user", columnList = "user_id"),
                @Index(name = "idx_user_trans_order", columnList = "order_id"),
                @Index(name = "idx_user_trans_symbol", columnList = "symbol"),
                @Index(name = "idx_user_trans_date", columnList = "transaction_date")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side; // 'BUY' or 'SELL'

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal commission;

    @Column(precision = 15, scale = 2)
    private BigDecimal tax;

    @Column(name = "net_amount", precision = 20, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(length = 50)
    private String status; // 'PENDING', 'SETTLED', 'FAILED'

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}

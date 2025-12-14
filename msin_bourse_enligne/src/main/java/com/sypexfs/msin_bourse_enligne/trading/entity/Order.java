package com.sypexfs.msin_bourse_enligne.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "orders", schema = "trading_schema",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_symbol", columnList = "symbol"),
                @Index(name = "idx_orders_status", columnList = "stat_id"),
                @Index(name = "idx_orders_date", columnList = "date_entry"),
                @Index(name = "idx_orders_user_status", columnList = "user_id, stat_id"),
                @Index(name = "idx_orders_symbol_date", columnList = "symbol, date_entry")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer sign; // 1 for BUY, -1 for SELL

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "stop_price", precision = 15, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "displayed_quantity", precision = 15, scale = 2)
    private BigDecimal displayedQuantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "order_amount", precision = 20, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "date_order")
    private LocalDate dateOrder;

    @Column(name = "date_expiry")
    private LocalDate dateExpiry;

    @Column(name = "time_order")
    private LocalTime timeOrder;

    @Column(name = "time_expiry")
    private LocalTime timeExpiry;

    @Column(name = "date_entry", nullable = false)
    private LocalDateTime dateEntry;

    @Column(name = "exec_qty", precision = 15, scale = 2)
    private BigDecimal execQty = BigDecimal.ZERO;

    @Column(name = "exec_avg_price", precision = 15, scale = 4)
    private BigDecimal execAvgPrice;

    @Column(name = "expiry_type_id", length = 50)
    private String expiryTypeId; // 'DAY', 'GTC', 'IOC', 'FOK'

    @Column(name = "stat_id", length = 50)
    private String statId; // 'PENDING', 'PARTIAL', 'FILLED', 'CANCELLED', 'REJECTED'

    @Column(name = "order_type_id", length = 50)
    private String orderTypeId; // 'MARKET', 'LIMIT', 'STOP', 'STOP_LIMIT'

    @Column(name = "broker_id")
    private Long brokerId;

    @Column(name = "cash_account_id")
    private Long cashAccountId;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "portf_id")
    private Long portfId;

    @Column(name = "sub_portf_id")
    private Long subPortfId;

    @Column(name = "sec_account_id")
    private Long secAccountId;

    @Column(name = "sec_id")
    private Long secId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(name = "sub_red_type_id", length = 50)
    private String subRedTypeId;

    @Column(name = "external_ref", length = 100)
    private String externalRef;

    @Column(name = "class_id", length = 50)
    private String classId;

    @Column(name = "account_type", length = 50)
    private String accountType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dateEntry == null) {
            dateEntry = LocalDateTime.now();
        }
    }

    public BigDecimal getRemainingQuantity() {
        if (quantity == null) return BigDecimal.ZERO;
        if (execQty == null) return quantity;
        return quantity.subtract(execQty);
    }

    public boolean isBuyOrder() {
        return sign != null && sign == 1;
    }

    public boolean isSellOrder() {
        return sign != null && sign == -1;
    }

    public boolean isPending() {
        return "PENDING".equals(statId);
    }

    public boolean isPartiallyFilled() {
        return "PARTIAL".equals(statId);
    }

    public boolean isFilled() {
        return "FILLED".equals(statId);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(statId);
    }

    public boolean isRejected() {
        return "REJECTED".equals(statId);
    }
}

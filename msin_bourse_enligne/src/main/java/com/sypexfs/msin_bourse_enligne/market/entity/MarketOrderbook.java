package com.sypexfs.msin_bourse_enligne.market.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_orderbook", schema = "market_schema",
        indexes = {
                @Index(name = "idx_orderbook_symbol", columnList = "symbol"),
                @Index(name = "idx_orderbook_side", columnList = "side"),
                @Index(name = "idx_orderbook_price", columnList = "price"),
                @Index(name = "idx_orderbook_symbol_side", columnList = "symbol, side, price")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketOrderbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sec_id", length = 50)
    private String secId;

    @Column(name = "market_place", length = 50)
    private String marketPlace;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side; // 'BUY' or 'SELL'

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "date_order")
    private LocalDateTime dateOrder;

    @Column(name = "order_market_id", unique = true, length = 100)
    private String orderMarketId;

    @Column(name = "order_type", length = 50)
    private String orderType;

    @Column(name = "is_own_order", nullable = false)
    private Boolean isOwnOrder = false;

    @Column(nullable = false)
    private Boolean delete = false;

    @Column(name = "delete_all", nullable = false)
    private Boolean deleteAll = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

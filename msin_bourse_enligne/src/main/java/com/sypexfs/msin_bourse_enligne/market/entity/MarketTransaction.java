package com.sypexfs.msin_bourse_enligne.market.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_transactions", schema = "market_schema",
        indexes = {
                @Index(name = "idx_transactions_symbol", columnList = "symbol"),
                @Index(name = "idx_transactions_date", columnList = "date_trans"),
                @Index(name = "idx_transactions_exec_id", columnList = "exec_id"),
                @Index(name = "idx_transactions_symbol_date", columnList = "symbol, date_trans")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_place", length = 50)
    private String marketPlace;

    @Column(name = "sec_id", length = 50)
    private String secId;

    @Column(name = "exec_id", unique = true, nullable = false, length = 100)
    private String execId;

    @Column(name = "exec_type", length = 50)
    private String execType;

    @Column(length = 10)
    private String side; // 'BUY' or 'SELL'

    @Column(name = "prev_exec_id", length = 100)
    private String prevExecId;

    @Column(name = "date_trans", nullable = false)
    private LocalDateTime dateTrans;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    @Column(length = 50)
    private String sequence;

    @Column(name = "trade_type", length = 50)
    private String tradeType;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(nullable = false)
    private Boolean cancel = false;

    @Column(name = "nano_second", precision = 20, scale = 9)
    private BigDecimal nanoSecond;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

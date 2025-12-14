package com.sypexfs.msin_bourse_enligne.market.entity;

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

@Entity
@Table(name = "market_instruments", schema = "market_schema",
        indexes = {
                @Index(name = "idx_instruments_symbol", columnList = "symbol"),
                @Index(name = "idx_instruments_sector", columnList = "sector"),
                @Index(name = "idx_instruments_trading_status", columnList = "trading_status"),
                @Index(name = "idx_instruments_market_segment", columnList = "market_segment")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketInstrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_place", length = 50)
    private String marketPlace;

    @Column(unique = true, nullable = false, length = 20)
    private String symbol;

    @Column(length = 255)
    private String name;

    @Column(name = "class_id", length = 50)
    private String classId;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "last_trade_time")
    private LocalDateTime lastTradeTime;

    @Column(length = 255)
    private String issuer;

    @Column(name = "market_segment", length = 50)
    private String marketSegment;

    @Column(name = "price_type", length = 50)
    private String priceType;

    @Column(name = "match_type", length = 50)
    private String matchType;

    @Column(name = "trading_type", length = 50)
    private String tradingType;

    @Column(name = "trading_status", length = 50)
    private String tradingStatus;

    @Column(length = 10)
    private String currency = "MAD";

    @Column(name = "halt_reason", length = 255)
    private String haltReason;

    @Column(name = "session_change_reason", length = 255)
    private String sessionChangeReason;

    @Column(name = "high_px", precision = 15, scale = 4)
    private BigDecimal highPx;

    @Column(name = "low_px", precision = 15, scale = 4)
    private BigDecimal lowPx;

    @Column(name = "last_px", precision = 15, scale = 4)
    private BigDecimal lastPx;

    @Column(name = "cum_qty", precision = 15, scale = 2)
    private BigDecimal cumQty;

    @Column(name = "identification_number", length = 50)
    private String identificationNumber;

    @Column(length = 50)
    private String underlying;

    @Column(name = "strike_price", precision = 15, scale = 4)
    private BigDecimal strikePrice;

    @Column(name = "option_type", length = 20)
    private String optionType;

    @Column(precision = 10, scale = 4)
    private BigDecimal coupon;

    @Column(name = "corporate_action", length = 255)
    private String corporateAction;

    @Column(length = 100)
    private String sector;

    @Column(name = "market_type", length = 50)
    private String marketType;

    @Column(name = "issued_qty", precision = 15, scale = 2)
    private BigDecimal issuedQty;

    @Column(name = "contract_multiplier", precision = 10, scale = 2)
    private BigDecimal contractMultiplier;

    @Column(name = "settlement_method", length = 50)
    private String settlementMethod;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

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
@Table(name = "market_summaries", schema = "market_schema",
        indexes = {
                @Index(name = "idx_summaries_symbol", columnList = "symbol"),
                @Index(name = "idx_summaries_sec_id", columnList = "sec_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sec_id", length = 50)
    private String secId;

    @Column(name = "market_place", length = 50)
    private String marketPlace;

    @Column(name = "open_close_indicator", length = 10)
    private String openCloseIndicator;

    @Column(length = 20)
    private String symbol;

    @Column(length = 255)
    private String name;

    @Column(name = "last_closing_price", precision = 15, scale = 4)
    private BigDecimal lastClosingPrice;

    @Column(precision = 15, scale = 4)
    private BigDecimal top;

    @Column(precision = 20, scale = 2)
    private BigDecimal tov;

    @Column(name = "closing_price", precision = 15, scale = 4)
    private BigDecimal closingPrice;

    @Column(name = "opening_price", precision = 15, scale = 4)
    private BigDecimal openingPrice;

    @Column(name = "date_trans")
    private LocalDateTime dateTrans;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    @Column(precision = 10, scale = 4)
    private BigDecimal variation;

    @Column(name = "higher_price", precision = 15, scale = 4)
    private BigDecimal higherPrice;

    @Column(name = "lower_price", precision = 15, scale = 4)
    private BigDecimal lowerPrice;

    @Column(name = "higher_limit", precision = 15, scale = 4)
    private BigDecimal higherLimit;

    @Column(name = "lower_limit", precision = 15, scale = 4)
    private BigDecimal lowerLimit;

    @Column(name = "static_higher_limit", precision = 15, scale = 4)
    private BigDecimal staticHigherLimit;

    @Column(name = "static_lower_limit", precision = 15, scale = 4)
    private BigDecimal staticLowerLimit;

    @Column(precision = 15, scale = 4)
    private BigDecimal vwap;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 2)
    private BigDecimal volume;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "notional_exposer", precision = 20, scale = 2)
    private BigDecimal notionalExposer;

    @Column(name = "underlying_ref_price", precision = 15, scale = 4)
    private BigDecimal underlyingRefPrice;

    @Column(name = "open_intrest", precision = 15, scale = 2)
    private BigDecimal openIntrest;

    @Column(name = "theoretical_price", precision = 15, scale = 4)
    private BigDecimal theoreticalPrice;

    @Column(name = "auction_qty", precision = 15, scale = 2)
    private BigDecimal auctionQty;

    @Column(name = "auction_imbalance_qty", precision = 15, scale = 2)
    private BigDecimal auctionImbalanceQty;

    @Column(name = "auction_price", precision = 15, scale = 4)
    private BigDecimal auctionPrice;

    @Column(name = "auction_type", length = 50)
    private String auctionType;

    @Column(name = "price_band_limit_sup", precision = 15, scale = 4)
    private BigDecimal priceBandLimitSup;

    @Column(name = "price_band_limit_inf", precision = 15, scale = 4)
    private BigDecimal priceBandLimitInf;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (symbol != null && !symbol.isEmpty()) {
            return symbol;
        }
        return "";
    }

    public boolean isPositive() {
        return variation != null && variation.compareTo(BigDecimal.ZERO) >= 0;
    }
}

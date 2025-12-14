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
@Table(name = "market_index_summaries", schema = "market_schema",
        indexes = {
                @Index(name = "idx_index_summaries_symbol", columnList = "symbol"),
                @Index(name = "idx_index_summaries_date", columnList = "date_price")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndexSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "closing_price", precision = 15, scale = 4)
    private BigDecimal closingPrice;

    @Column(name = "opening_price", precision = 15, scale = 4)
    private BigDecimal openingPrice;

    @Column(name = "last_closing_price", precision = 15, scale = 4)
    private BigDecimal lastClosingPrice;

    @Column(name = "lower_price", precision = 15, scale = 4)
    private BigDecimal lowerPrice;

    @Column(name = "higher_price", precision = 15, scale = 4)
    private BigDecimal higherPrice;

    @Column(precision = 10, scale = 4)
    private BigDecimal variation;

    @Column(name = "date_price")
    private LocalDateTime datePrice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isPositive() {
        return variation != null && variation.compareTo(BigDecimal.ZERO) >= 0;
    }
}

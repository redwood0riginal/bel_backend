package com.sypexfs.msin_bourse_enligne.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_positions", schema = "portfolio_schema",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"portfolio_id", "symbol"})
        },
        indexes = {
                @Index(name = "idx_positions_portfolio", columnList = "portfolio_id"),
                @Index(name = "idx_positions_symbol", columnList = "symbol")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(length = 255)
    private String libelle;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "average_cost", precision = 15, scale = 4)
    private BigDecimal averageCost; // cmp_net

    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice; // cours_marche

    @Column(name = "market_value", precision = 20, scale = 2)
    private BigDecimal marketValue; // valorisation

    @Column(name = "unrealized_pnl", precision = 20, scale = 2)
    private BigDecimal unrealizedPnl; // values_latentes

    @Column(name = "performance_percent", precision = 10, scale = 4)
    private BigDecimal performancePercent; // performance

    @Column(name = "weight_percent", precision = 10, scale = 4)
    private BigDecimal weightPercent; // poids

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        calculateMetrics();
    }

    private void calculateMetrics() {
        // Calculate market value
        if (quantity != null && currentPrice != null) {
            marketValue = quantity.multiply(currentPrice);
        }

        // Calculate unrealized P&L
        if (quantity != null && currentPrice != null && averageCost != null) {
            BigDecimal costBasis = quantity.multiply(averageCost);
            unrealizedPnl = marketValue.subtract(costBasis);

            // Calculate performance percentage
            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                performancePercent = unrealizedPnl
                        .divide(costBasis, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }
    }
}

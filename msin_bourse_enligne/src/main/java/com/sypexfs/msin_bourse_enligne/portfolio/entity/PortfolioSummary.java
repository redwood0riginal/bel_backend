package com.sypexfs.msin_bourse_enligne.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_summary", schema = "portfolio_schema")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", unique = true, nullable = false)
    private Portfolio portfolio;

    @Column(name = "total_value", precision = 20, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "securities_value", precision = 20, scale = 2)
    private BigDecimal securitiesValue;

    @Column(name = "cash_balance", precision = 20, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "invested_amount", precision = 20, scale = 2)
    private BigDecimal investedAmount;

    @Column(name = "total_pnl", precision = 20, scale = 2)
    private BigDecimal totalPnl;

    @Column(name = "total_pnl_percent", precision = 10, scale = 4)
    private BigDecimal totalPnlPercent;

    @Column(name = "day_pnl", precision = 20, scale = 2)
    private BigDecimal dayPnl;

    @Column(name = "day_pnl_percent", precision = 10, scale = 4)
    private BigDecimal dayPnlPercent;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}

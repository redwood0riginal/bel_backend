package com.sypexfs.msin_bourse_enligne.portfolio.entity;

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
@Table(name = "cash_accounts", schema = "portfolio_schema",
        indexes = {
                @Index(name = "idx_cash_accounts_portfolio", columnList = "portfolio_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    private String accountNumber;

    @Column(precision = 20, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 20, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "blocked_balance", precision = 20, scale = 2)
    private BigDecimal blockedBalance = BigDecimal.ZERO;

    @Column(length = 10)
    private String currency = "MAD";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void credit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount);
            availableBalance = availableBalance.add(amount);
        }
    }

    public void debit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.subtract(amount);
            availableBalance = availableBalance.subtract(amount);
        }
    }

    public void block(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            availableBalance = availableBalance.subtract(amount);
            blockedBalance = blockedBalance.add(amount);
        }
    }

    public void unblock(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            availableBalance = availableBalance.add(amount);
            blockedBalance = blockedBalance.subtract(amount);
        }
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return availableBalance != null && 
               amount != null && 
               availableBalance.compareTo(amount) >= 0;
    }
}

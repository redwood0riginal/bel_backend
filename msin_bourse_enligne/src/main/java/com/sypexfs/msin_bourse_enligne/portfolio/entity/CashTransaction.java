package com.sypexfs.msin_bourse_enligne.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_transactions", schema = "portfolio_schema",
        indexes = {
                @Index(name = "idx_cash_trans_account", columnList = "cash_account_id"),
                @Index(name = "idx_cash_trans_date", columnList = "transaction_date"),
                @Index(name = "idx_cash_trans_type", columnList = "transaction_type")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_account_id", nullable = false)
    private CashAccount cashAccount;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // 'DEPOSIT', 'WITHDRAWAL', 'TRADE_BUY', 'TRADE_SELL', 'DIVIDEND', 'FEE'

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 20, scale = 2)
    private BigDecimal balanceAfter;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    public boolean isCredit() {
        return "DEPOSIT".equals(transactionType) || 
               "TRADE_SELL".equals(transactionType) || 
               "DIVIDEND".equals(transactionType);
    }

    public boolean isDebit() {
        return "WITHDRAWAL".equals(transactionType) || 
               "TRADE_BUY".equals(transactionType) || 
               "FEE".equals(transactionType);
    }
}

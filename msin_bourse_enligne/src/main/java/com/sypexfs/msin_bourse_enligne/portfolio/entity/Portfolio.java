package com.sypexfs.msin_bourse_enligne.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios", schema = "portfolio_schema",
        indexes = {
                @Index(name = "idx_portfolios_user", columnList = "user_id"),
                @Index(name = "idx_portfolios_status", columnList = "status")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_type", length = 50)
    private String accountType; // 'CASH', 'MARGIN', 'RETIREMENT'

    @Column(length = 10)
    private String currency = "MAD";

    @Column(length = 50)
    private String status = "ACTIVE";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}

package com.sypexfs.msin_bourse_enligne.market.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "market_news", schema = "market_schema",
        indexes = {
                @Index(name = "idx_news_published", columnList = "published_at")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String headline;

    @Column(name = "encoded_headline_len", length = 50)
    private String encodedHeadlineLen;

    @Column(name = "encoded_headline", columnDefinition = "TEXT")
    private String encodedHeadline;

    @Column(length = 20)
    private String urgency;

    @Column(name = "no_lines_of_text", length = 10)
    private String noLinesOfText;

    @Column(name = "lines_of_text", columnDefinition = "TEXT[]")
    private String[] linesOfText;

    @Column(name = "url_link", length = 500)
    private String urlLink;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

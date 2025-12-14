package com.sypexfs.msin_bourse_enligne.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDto {

    private Long id;
    private String headline;
    private String urgency;
    private String[] linesOfText;
    private String urlLink;
    private LocalDateTime publishedAt;
}

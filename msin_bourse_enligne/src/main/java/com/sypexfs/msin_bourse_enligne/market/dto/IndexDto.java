package com.sypexfs.msin_bourse_enligne.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexDto {

    private Long id;
    private String code;
    private String name;
    private String marketPlace;
    private String indexType;
    private BigDecimal price;
    private LocalDateTime datePrice;
}

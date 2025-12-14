package com.sypexfs.msin_bourse_enligne.market.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndexResponseDto {

    @JsonProperty("objectType")
    private String objectType = "MarketIndex";

    @JsonProperty("id")
    private Long id;

    @JsonProperty("indexId")
    private String indexId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("indexType")
    private String indexType;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("datePrice")
    private Date datePrice;
}

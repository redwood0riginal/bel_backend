package com.sypexfs.msin_bourse_enligne.trading.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Stop price must be greater than 0")
    private BigDecimal stopPrice;

    private BigDecimal displayedQuantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private LocalDate dateExpiry;

    private LocalTime timeExpiry;

    private String expiryTypeId;
}

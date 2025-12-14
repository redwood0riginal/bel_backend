package com.sypexfs.msin_bourse_enligne.trading.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "Sign is required")
    @Min(value = -1, message = "Sign must be 1 (BUY) or -1 (SELL)")
    @Max(value = 1, message = "Sign must be 1 (BUY) or -1 (SELL)")
    private Integer sign; // 1 for BUY, -1 for SELL

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Stop price must be greater than 0")
    private BigDecimal stopPrice;

    private BigDecimal displayedQuantity;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private LocalDate dateOrder;

    private LocalDate dateExpiry;

    private LocalTime timeOrder;

    private LocalTime timeExpiry;

    @NotBlank(message = "Expiry type is required")
    @Pattern(regexp = "DAY|GTC|IOC|FOK", message = "Expiry type must be DAY, GTC, IOC, or FOK")
    private String expiryTypeId; // 'DAY', 'GTC', 'IOC', 'FOK'

    @NotBlank(message = "Order type is required")
    @Pattern(regexp = "MARKET|LIMIT|STOP|STOP_LIMIT", message = "Order type must be MARKET, LIMIT, STOP, or STOP_LIMIT")
    private String orderTypeId; // 'MARKET', 'LIMIT', 'STOP', 'STOP_LIMIT'

    private Long brokerId;

    private Long cashAccountId;

    private Long entityId;

    private Long portfId;

    private Long subPortfId;

    private Long secAccountId;

    private Long secId;

    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    private String subRedTypeId;

    private String externalRef;

    private String classId;

    private String accountType;
}

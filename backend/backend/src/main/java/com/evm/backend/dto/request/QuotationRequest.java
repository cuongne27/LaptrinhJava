package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuotationRequest {

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Customer ID không được để trống")
    private Long customerId;

    @NotNull(message = "Dealer ID không được để trống")
    private Long dealerId;

    private Long salesPersonId; // Optional, có thể auto-assign

    private LocalDate quotationDate;
    private LocalDate validUntil;

    @NotNull(message = "Giá cơ bản không được để trống")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePrice;

    private BigDecimal registrationFee;

    private Set<Long> promotionIds; // Các khuyến mãi áp dụng

    private String notes;
    private String termsAndConditions;
}
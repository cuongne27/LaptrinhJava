package com.evm.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuotationResponse {
    private Long id;
    private String quotationNumber;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private String status;

    // Price breakdown
    private BigDecimal basePrice;
    private BigDecimal vat;
    private BigDecimal registrationFee;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;

    // Product info
    private Long productId;
    private String productName;
    private String productVersion;
    private String productImageUrl;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Sales person info
    private Long salesPersonId;
    private String salesPersonName;

    // Dealer info
    private Long dealerId;
    private String dealerName;

    // Promotions applied
    private List<PromotionSummary> promotions;

    private String notes;
    private String termsAndConditions;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Status indicators
    private Boolean isExpired;
    private Integer daysUntilExpiry;
    private Boolean canConvertToOrder;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PromotionSummary {
        private Long promotionId;
        private String promotionName;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal appliedAmount;
    }
}
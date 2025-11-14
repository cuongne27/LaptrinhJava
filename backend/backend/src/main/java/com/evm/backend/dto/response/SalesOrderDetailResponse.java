package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO for SalesOrder detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderDetailResponse {
    private Long id;
    private LocalDate orderDate;
    private BigDecimal basePrice;
    private BigDecimal vat;
    private BigDecimal registrationFee;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private String status;

    // Vehicle info
    private String vehicleId;
    private String vehicleModel;
    private String vehicleBrand;
    private String vehicleVin;
    private String vehicleColor;
    private Integer vehicleYear;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    // Sales person info
    private Long salesPersonId;
    private String salesPersonName;
    private String salesPersonEmail;
    private String salesPersonPhone;

    // Related data
    private Set<PaymentSummary> payments;
    private Set<PromotionSummary> promotions;

    // Calculated fields
    private Integer daysFromOrder;
    private Boolean isPaid;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Boolean canCancel; // Có thể hủy không

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentSummary {
        private Long paymentId;
        private LocalDate paymentDate;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PromotionSummary {
        private Long promotionId;
        private String promotionName;
        private String discountType;
        private BigDecimal discountValue;
    }
}
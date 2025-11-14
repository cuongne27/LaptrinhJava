package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Payment Detail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailResponse {
    private Long paymentId;

    // Order info
    private Long orderId;
    private String orderReference;
    private BigDecimal orderTotalAmount;
    private BigDecimal orderPaidAmount;
    private BigDecimal orderRemainingAmount;

    // Payment info
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;
    private String paymentType;
    private String referenceNumber;

    // Payer info (người thanh toán)
    private Long payerId;
    private String payerName;
    private String payerEmail;
    private String payerPhone;

    // Customer info (khách hàng đặt hàng)
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Vehicle info
    private String vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
}
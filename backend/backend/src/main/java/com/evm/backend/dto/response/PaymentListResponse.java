package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Payment List
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentListResponse {
    private Long paymentId;
    private Long orderId;
    private String orderReference; // Mã đơn hàng
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;
    private String paymentType;
    private String referenceNumber;

    // Payer info
    private Long payerId;
    private String payerName;

    // Customer info (order customer)
    private Long customerId;
    private String customerName;
}
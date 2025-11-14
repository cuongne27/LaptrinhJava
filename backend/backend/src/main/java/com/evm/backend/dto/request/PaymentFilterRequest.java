package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for filtering payments
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFilterRequest {

    private Long orderId; // Filter by order
    private Long customerId; // Filter by customer
    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD, INSTALLMENT
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    private String paymentType; // ORDER_PAYMENT, DEPOSIT, INSTALLMENT, FINAL_PAYMENT
    private LocalDate fromDate; // Filter from date
    private LocalDate toDate; // Filter to date
    private String referenceNumber; // Search by reference number

    // Sorting: date_asc, date_desc, amount_asc, amount_desc
    private String sortBy;

    private Integer page;
    private Integer size;
}
package com.evm.backend.service;

import com.evm.backend.dto.request.PaymentFilterRequest;
import com.evm.backend.dto.request.PaymentRequest;
import com.evm.backend.dto.response.PaymentDetailResponse;
import com.evm.backend.dto.response.PaymentListResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Payment operations
 * Handles all payment-related business logic for sales orders
 */
public interface PaymentService {

    /**
     * Get all payments with filtering and pagination
     */
    Page<PaymentListResponse> getAllPayments(PaymentFilterRequest filterRequest);

    /**
     * Get payments by order ID
     */
    List<PaymentListResponse> getPaymentsByOrderId(Long orderId);

    /**
     * Get payments by customer ID
     */
    List<PaymentListResponse> getPaymentsByCustomerId(Long customerId);

    /**
     * Get pending payments
     */
    List<PaymentListResponse> getPendingPayments();

    /**
     * Get payment detail by ID
     */
    PaymentDetailResponse getPaymentById(Long paymentId);

    /**
     * Get payment by reference number
     */
    PaymentDetailResponse getPaymentByReferenceNumber(String referenceNumber);

    /**
     * Create new payment
     */
    PaymentDetailResponse createPayment(PaymentRequest request);

    /**
     * Update payment
     */
    PaymentDetailResponse updatePayment(Long paymentId, PaymentRequest request);

    /**
     * Confirm payment (change status to COMPLETED)
     */
    PaymentDetailResponse confirmPayment(Long paymentId);

    /**
     * Cancel/Refund payment
     */
    PaymentDetailResponse refundPayment(Long paymentId, String reason);

    /**
     * Delete payment (admin only)
     */
    void deletePayment(Long paymentId);

    /**
     * Get total paid amount for an order
     */
    BigDecimal getTotalPaidAmountByOrderId(Long orderId);

    /**
     * Get payment statistics by date range
     */
    List<PaymentListResponse> getPaymentStatistics(String fromDate, String toDate);
}
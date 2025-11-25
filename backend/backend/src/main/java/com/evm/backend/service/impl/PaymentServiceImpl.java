package com.evm.backend.service.impl;

import com.evm.backend.dto.request.PaymentFilterRequest;
import com.evm.backend.dto.request.PaymentRequest;
import com.evm.backend.dto.response.PaymentDetailResponse;
import com.evm.backend.dto.response.PaymentListResponse;
import com.evm.backend.entity.Payment;
import com.evm.backend.entity.SalesOrder;
import com.evm.backend.entity.Customer;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.repository.PaymentRepository;
import com.evm.backend.repository.SalesOrderRepository;
import com.evm.backend.repository.CustomerRepository;
import com.evm.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getAllPayments(PaymentFilterRequest filterRequest) {
        log.info("Getting all payments with filter: {}", filterRequest);

        Specification<Payment> spec = buildSpecification(filterRequest);
        Pageable pageable = buildPageable(filterRequest);

        Page<Payment> payments = paymentRepository.findAll(spec, pageable);

        return payments.map(this::convertToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentListResponse> getPaymentsByOrderId(Long orderId) {
        log.info("Getting payments for order: {}", orderId);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        List<Payment> payments = paymentRepository.findByOrderIdOrderByPaymentDateDesc(orderId);

        return payments.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentListResponse> getPaymentsByCustomerId(Long customerId) {
        log.info("Getting payments for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        List<Payment> payments = paymentRepository.findByCustomerId(customerId);

        return payments.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentListResponse> getPendingPayments() {
        log.info("Getting pending payments");

        List<Payment> payments = paymentRepository.findByStatusOrderByPaymentDateDesc("PENDING");

        return payments.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentById(Long paymentId) {
        log.info("Getting payment by id: {}", paymentId);

        Payment payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        return convertToDetailResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentByReferenceNumber(String referenceNumber) {
        log.info("Getting payment by reference number: {}", referenceNumber);

        Payment payment = paymentRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + referenceNumber));

        return convertToDetailResponse(payment);
    }

    @Override
    public PaymentDetailResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order: {}", request.getOrderId());

        // Validate order exists
        SalesOrder order = salesOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        // Validate payment amount
        validatePaymentAmount(order, request.getAmount());

        // Get payer (if specified, otherwise use order's customer)
        Customer payer = null;
        if (request.getPayerId() != null) {
            payer = customerRepository.findById(request.getPayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payer not found: " + request.getPayerId()));
        } else {
            payer = order.getCustomer();
        }

        // Create payment entity
        Payment payment = Payment.builder()
                .order(order)
                .payer(payer)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .paymentType(request.getPaymentType())
                .referenceNumber(request.getReferenceNumber())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully: {}", savedPayment.getId());

        // Update order status if fully paid
        updateOrderStatusIfFullyPaid(order);

        return convertToDetailResponse(savedPayment);
    }

    @Override
    public PaymentDetailResponse updatePayment(Long paymentId, PaymentRequest request) {
        log.info("Updating payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        // Validate order
        SalesOrder order = salesOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        // Get payer
        Customer payer = null;
        if (request.getPayerId() != null) {
            payer = customerRepository.findById(request.getPayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payer not found: " + request.getPayerId()));
        } else {
            payer = order.getCustomer();
        }

        // Update fields
        payment.setOrder(order);
        payment.setPayer(payer);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(request.getStatus() != null ? request.getStatus() : payment.getStatus());
        payment.setPaymentType(request.getPaymentType());
        payment.setReferenceNumber(request.getReferenceNumber());

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment updated successfully: {}", paymentId);

        // Update order status if needed
        updateOrderStatusIfFullyPaid(order);

        return convertToDetailResponse(updatedPayment);
    }

    @Override
    public PaymentDetailResponse confirmPayment(Long paymentId) {
        log.info("Confirming payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new BadRequestException("Payment is already confirmed");
        }

        if ("REFUNDED".equals(payment.getStatus())) {
            throw new BadRequestException("Cannot confirm refunded payment");
        }

        payment.setStatus("COMPLETED");
        Payment confirmedPayment = paymentRepository.save(payment);
        log.info("Payment confirmed successfully: {}", paymentId);

        // Update order status if fully paid
        updateOrderStatusIfFullyPaid(payment.getOrder());

        return convertToDetailResponse(confirmedPayment);
    }

    @Override
    public PaymentDetailResponse refundPayment(Long paymentId, String reason) {
        log.info("Refunding payment: {} - Reason: {}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if ("REFUNDED".equals(payment.getStatus())) {
            throw new BadRequestException("Payment is already refunded");
        }

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new BadRequestException("Only completed payments can be refunded");
        }

        payment.setStatus("REFUNDED");

        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Payment refunded successfully: {}", paymentId);

        // Update order status
        updateOrderStatusAfterRefund(payment.getOrder());

        return convertToDetailResponse(refundedPayment);
    }

    @Override
    @Transactional
    public void deletePayment(Long paymentId) {
        log.info("Deleting payment: {}", paymentId);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

            if ("COMPLETED".equals(payment.getStatus())) {
                throw new BadRequestException("Cannot delete completed payment. Please refund it first.");
            }

            // ✅ LƯU THÔNG TIN CẦN THIẾT TRƯỚC KHI XÓA
            Long orderId = payment.getOrder() != null ? payment.getOrder().getId() : null;

            // ✅ XÓA VỚI ERROR HANDLING RÕ RÀNG
            paymentRepository.delete(payment);
            paymentRepository.flush(); // Force thực thi DELETE ngay lập tức

            log.info("Payment deleted successfully: {}", paymentId);

            // ✅ CẬP NHẬT TRẠNG THÁI ORDER
            if (orderId != null) {
                try {
                    SalesOrder managedOrder = salesOrderRepository.findById(orderId)
                            .orElse(null);
                    if (managedOrder != null) {
                        updateOrderStatusAfterRefund(managedOrder);
                        log.info("Order {} status updated after payment deletion", orderId);
                    }
                } catch (Exception e) {
                    log.error("Failed to update order status after payment deletion: {}", e.getMessage());
                    // Không throw lỗi này vì payment đã xóa thành công
                }
            }

        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete payment due to foreign key constraint: {}", e.getMessage());
            throw new BadRequestException(
                    "Cannot delete this payment because it is referenced by other records. " +
                            "Please refund it instead of deleting."
            );
        } catch (Exception e) {
            log.error("Error deleting payment {}: {}", paymentId, e.getMessage(), e);
            throw new BadRequestException("Failed to delete payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmountByOrderId(Long orderId) {
        log.info("Getting total paid amount for order: {}", orderId);

        List<Payment> completedPayments = paymentRepository.findByOrderIdAndStatus(orderId, "COMPLETED");

        return completedPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentListResponse> getPaymentStatistics(String fromDate, String toDate) {
        log.info("Getting payment statistics from {} to {}", fromDate, toDate);

        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);

        List<Payment> payments = paymentRepository.findByPaymentDateBetweenAndStatus(from, to, "COMPLETED");

        return payments.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    // ===== HELPER METHODS =====

    private Specification<Payment> buildSpecification(PaymentFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getOrderId() != null) {
                predicates.add(cb.equal(root.get("order").get("id"), filter.getOrderId()));
            }

            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("payer").get("id"), filter.getCustomerId()));
            }

            if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isEmpty()) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPaymentType() != null && !filter.getPaymentType().isEmpty()) {
                predicates.add(cb.equal(root.get("paymentType"), filter.getPaymentType()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), filter.getToDate()));
            }

            if (filter.getReferenceNumber() != null && !filter.getReferenceNumber().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("referenceNumber")),
                        "%" + filter.getReferenceNumber().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(PaymentFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? Math.min(filter.getSize(), 100) : 20;

        Sort sort = Sort.by(Sort.Direction.DESC, "paymentDate");

        if (filter.getSortBy() != null) {
            switch (filter.getSortBy()) {
                case "date_asc":
                    sort = Sort.by(Sort.Direction.ASC, "paymentDate");
                    break;
                case "date_desc":
                    sort = Sort.by(Sort.Direction.DESC, "paymentDate");
                    break;
                case "amount_asc":
                    sort = Sort.by(Sort.Direction.ASC, "amount");
                    break;
                case "amount_desc":
                    sort = Sort.by(Sort.Direction.DESC, "amount");
                    break;
                default:
                    break;
            }
        }

        return PageRequest.of(page, size, sort);
    }

    private void validatePaymentAmount(SalesOrder order, BigDecimal paymentAmount) {
        BigDecimal totalPaid = getTotalPaidAmountByOrderId(order.getId());
        BigDecimal remaining = order.getTotalPrice().subtract(totalPaid);

        if (paymentAmount.compareTo(remaining) > 0) {
            throw new BadRequestException(
                    String.format("Payment amount %.2f exceeds remaining amount %.2f",
                            paymentAmount, remaining)
            );
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than 0");
        }
    }

    private void updateOrderStatusIfFullyPaid(SalesOrder order) {
        BigDecimal totalPaid = getTotalPaidAmountByOrderId(order.getId());
        BigDecimal remaining = order.getTotalPrice().subtract(totalPaid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus("PAID");
            salesOrderRepository.save(order);
            log.info("Order {} marked as PAID", order.getId());
        }
    }

    private void updateOrderStatusAfterRefund(SalesOrder order) {
        BigDecimal totalPaid = getTotalPaidAmountByOrderId(order.getId());

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus("PENDING");
        } else if (totalPaid.compareTo(order.getTotalPrice()) < 0) {
            order.setStatus("CONFIRMED");
        }

        salesOrderRepository.save(order);
        log.info("Order {} status updated after refund", order.getId());
    }

    private PaymentListResponse convertToListResponse(Payment p) {
        return PaymentListResponse.builder()
                .paymentId(p.getId())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .orderReference(p.getOrder() != null ? "ORD-" + p.getOrder().getId() : null)
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .paymentType(p.getPaymentType())
                .referenceNumber(p.getReferenceNumber())
                // Payer info
                .payerId(p.getPayer() != null ? p.getPayer().getId() : null)
                .payerName(p.getPayer() != null ? p.getPayer().getFullName() : null)
                // Order customer info
                .customerId(p.getOrder() != null && p.getOrder().getCustomer() != null ?
                        p.getOrder().getCustomer().getId() : null)
                .customerName(p.getOrder() != null && p.getOrder().getCustomer() != null ?
                        p.getOrder().getCustomer().getFullName() : null)
                .build();
    }

    private PaymentDetailResponse convertToDetailResponse(Payment p) {
        SalesOrder order = p.getOrder();
        BigDecimal totalPaid = getTotalPaidAmountByOrderId(order.getId());
        BigDecimal remaining = order.getTotalPrice().subtract(totalPaid);

        return PaymentDetailResponse.builder()
                .paymentId(p.getId())
                // Order info
                .orderId(order.getId())
                .orderReference("ORD-" + order.getId())
                .orderTotalAmount(order.getTotalPrice())
                .orderPaidAmount(totalPaid)
                .orderRemainingAmount(remaining)
                // Payment info
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .paymentType(p.getPaymentType())
                .referenceNumber(p.getReferenceNumber())
                // Payer info
                .payerId(p.getPayer() != null ? p.getPayer().getId() : null)
                .payerName(p.getPayer() != null ? p.getPayer().getFullName() : null)
                .payerEmail(p.getPayer() != null ? p.getPayer().getEmail() : null)
                .payerPhone(p.getPayer() != null ? p.getPayer().getPhoneNumber() : null)
                // Customer info (order customer)
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhoneNumber() : null)
                // Vehicle info
                .vehicleId(order.getVehicle() != null ? order.getVehicle().getId() : null)
                .vehicleBrand(order.getVehicle() != null && order.getVehicle().getProduct() != null &&
                        order.getVehicle().getProduct().getBrand() != null ?
                        order.getVehicle().getProduct().getBrand().getBrandName() : null)
                .vehicleModel(order.getVehicle() != null && order.getVehicle().getProduct() != null ?
                        order.getVehicle().getProduct().getProductName() : null)
                .build();
    }
}
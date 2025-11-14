package com.evm.backend.repository;

import com.evm.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    /**
     * Find payment by ID with all related entities (order, customer, vehicle)
     */
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.order o " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH o.vehicle v " +
            "LEFT JOIN FETCH v.product prod " +
            "LEFT JOIN FETCH prod.brand b " +
            "WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);

    /**
     * Find payments by order ID, ordered by payment date descending
     */
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.paymentDate DESC")
    List<Payment> findByOrderIdOrderByPaymentDateDesc(@Param("orderId") Long orderId);

    /**
     * Find payments by payer ID (customer ID), ordered by payment date descending
     */
    @Query("SELECT p FROM Payment p WHERE p.payer.id = :payerId ORDER BY p.paymentDate DESC")
    List<Payment> findByPayerIdOrderByPaymentDateDesc(@Param("payerId") Long payerId);

    /**
     * Find payments by status, ordered by payment date descending
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.paymentDate DESC")
    List<Payment> findByStatusOrderByPaymentDateDesc(@Param("status") String status);

    /**
     * Find payment by reference number
     */
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.order o " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH o.vehicle v " +
            "WHERE p.referenceNumber = :referenceNumber")
    Optional<Payment> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);

    /**
     * Find payments by order ID and status
     */
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status = :status")
    List<Payment> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * Find payments by payment date between and status
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :fromDate AND :toDate " +
            "AND p.status = :status " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetweenAndStatus(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    /**
     * Find payments by payment method
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :paymentMethod ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentMethodOrderByPaymentDateDesc(@Param("paymentMethod") String paymentMethod);

    /**
     * Find payments by payment type
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentType = :paymentType ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentTypeOrderByPaymentDateDesc(@Param("paymentType") String paymentType);

    /**
     * Get total paid amount for an order (sum of COMPLETED payments)
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.order.id = :orderId AND p.status = 'COMPLETED'")
    java.math.BigDecimal getTotalPaidAmountByOrderId(@Param("orderId") Long orderId);

    /**
     * Count payments by status
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);

    /**
     * Get total payment amount by status and date range
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.status = :status " +
            "AND p.paymentDate BETWEEN :fromDate AND :toDate")
    java.math.BigDecimal getTotalAmountByStatusAndDateRange(
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Find recent payments (last N days)
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.paymentDate >= :fromDate " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findRecentPayments(@Param("fromDate") LocalDate fromDate);

    /**
     * Find payments by customer (through order)
     */
    @Query("SELECT p FROM Payment p " +
            "JOIN p.order o " +
            "WHERE o.customer.id = :customerId " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Check if payment exists by reference number
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p " +
            "WHERE p.referenceNumber = :referenceNumber")
    boolean existsByReferenceNumber(@Param("referenceNumber") String referenceNumber);

    /**
     * Get payments for a specific vehicle
     */
    @Query("SELECT p FROM Payment p " +
            "JOIN p.order o " +
            "WHERE o.vehicle.id = :vehicleId " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * Get payment statistics by payment method
     */
    @Query("SELECT p.paymentMethod as method, COUNT(p) as count, SUM(p.amount) as total " +
            "FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "GROUP BY p.paymentMethod")
    List<Object[]> getPaymentStatisticsByMethod();

    /**
     * Get monthly payment statistics
     */
    @Query("SELECT YEAR(p.paymentDate) as year, MONTH(p.paymentDate) as month, " +
            "COUNT(p) as count, SUM(p.amount) as total " +
            "FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "GROUP BY YEAR(p.paymentDate), MONTH(p.paymentDate) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyPaymentStatistics();

    /**
     * Find failed payments that need retry
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.status = 'FAILED' " +
            "AND p.paymentDate >= :fromDate " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findFailedPaymentsForRetry(@Param("fromDate") LocalDate fromDate);

    /**
     * Get pending payments older than N days
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.status = 'PENDING' " +
            "AND p.paymentDate < :beforeDate " +
            "ORDER BY p.paymentDate ASC")
    List<Payment> findOldPendingPayments(@Param("beforeDate") LocalDate beforeDate);
}
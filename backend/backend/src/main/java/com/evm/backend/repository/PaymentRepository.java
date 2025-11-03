package com.evm.backend.repository;

import com.evm.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPayer(Customer payer);

    List<Payment> findByPayerId(Long payerId);

    List<Payment> findByPaymentMethod(String paymentMethod);

//    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
//    List<Payment> findByDateRange(@Param("startDate") LocalDate startDate,
//                                  @Param("endDate") LocalDate endDate);
//
//    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.salesOrder.id = :orderId AND p.status = 'COMPLETED'")
//    BigDecimal getTotalPaidForOrder(@Param("orderId") Long orderId);
//
//    @Query("SELECT p FROM Payment p WHERE p.paymentContext = :context AND p.status = 'PENDING'")
//    List<Payment> findPendingByContext(@Param("context") PaymentContext context);
//
//    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentContext = 'RETAIL_SALE' AND p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
//    BigDecimal getTotalRetailRevenue(@Param("startDate") LocalDate startDate,
//                                     @Param("endDate") LocalDate endDate);
}
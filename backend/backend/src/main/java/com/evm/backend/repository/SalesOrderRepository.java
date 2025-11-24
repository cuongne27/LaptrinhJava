package com.evm.backend.repository;

import com.evm.backend.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    /**
     * Tìm tất cả orders của customer
     */
    List<SalesOrder> findByCustomerId(Long customerId);

    /**
     * Tìm tất cả orders của sales person
     */
    List<SalesOrder> findBySalesPersonId(Long salesPersonId);

    /**
     * Tìm tất cả orders của vehicle
     */
    List<SalesOrder> findByVehicleId(String vehicleId);

    /**
     * Tìm orders theo status
     */
    List<SalesOrder> findByStatus(String status);

    /**
     * Tìm orders với filter
     */
    // Repository
    @Query("""
    SELECT DISTINCT o FROM SalesOrder o
    LEFT JOIN FETCH o.vehicle v
    LEFT JOIN FETCH v.product p
    LEFT JOIN FETCH p.brand b
    LEFT JOIN FETCH o.customer c
    LEFT JOIN FETCH o.salesPerson sp
    WHERE (:customerId IS NULL OR o.customer.id = :customerId)
    AND (:salesPersonId IS NULL OR o.salesPerson.id = :salesPersonId)
    AND (:vehicleId IS NULL OR o.vehicle.id = :vehicleId)
    AND (:status IS NULL OR o.status = :status)
    AND (:fromDate IS NULL OR o.orderDate >= :fromDate)
    AND (:toDate IS NULL OR o.orderDate <= :toDate)
    ORDER BY o.orderDate DESC
    """)
    List<SalesOrder> findOrdersWithFilters(
            @Param("customerId") Long customerId,
            @Param("salesPersonId") Long salesPersonId,
            @Param("vehicleId") String vehicleId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // Count query riêng
    @Query("""
    SELECT COUNT(DISTINCT o.id) FROM SalesOrder o
    WHERE (:customerId IS NULL OR o.customer.id = :customerId)
    AND (:salesPersonId IS NULL OR o.salesPerson.id = :salesPersonId)
    AND (:vehicleId IS NULL OR o.vehicle.id = :vehicleId)
    AND (:status IS NULL OR o.status = :status)
    AND (:fromDate IS NULL OR o.orderDate >= :fromDate)
    AND (:toDate IS NULL OR o.orderDate <= :toDate)
    """)
    Long countOrdersWithFilters(
            @Param("customerId") Long customerId,
            @Param("salesPersonId") Long salesPersonId,
            @Param("vehicleId") String vehicleId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Tìm order với đầy đủ thông tin (Bao gồm các mối quan hệ lồng nhau)
     */
    @Query("SELECT DISTINCT o FROM SalesOrder o " +
            "LEFT JOIN FETCH o.vehicle v " +          // Tải Vehicle
            "LEFT JOIN FETCH v.product p " +          // Tải Product (từ Vehicle)
            "LEFT JOIN FETCH p.brand b " +            // Tải Brand (từ Product)
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.salesPerson " +
            "LEFT JOIN FETCH o.payments " +
            "LEFT JOIN FETCH o.orderPromotions op " + // Tải OrderPromotion
            "LEFT JOIN FETCH op.promotion " +         // Tải Promotion (từ OrderPromotion)
            "WHERE o.id = :orderId")
    Optional<SalesOrder> findByIdWithDetails(@Param("orderId") Long orderId);

    /**
     * Kiểm tra vehicle đã được bán chưa
     */
    @Query("SELECT COUNT(o) > 0 FROM SalesOrder o " +
            "WHERE o.vehicle.id = :vehicleId " +
            "AND o.status IN ('PAID', 'COMPLETED')")
    boolean existsByVehicleIdAndSold(@Param("vehicleId") String vehicleId);

    /**
     * Lấy tổng doanh số theo sales person
     */
    @Query("SELECT SUM(o.totalPrice) FROM SalesOrder o " +
            "WHERE o.salesPerson.id = :salesPersonId " +
            "AND o.status IN ('PAID', 'COMPLETED')")
    Optional<BigDecimal> getTotalSalesBySalesPerson(@Param("salesPersonId") Long salesPersonId);

    /**
     * Lấy báo cáo doanh số theo tháng
     */
    @Query("SELECT o FROM SalesOrder o " +
            "WHERE YEAR(o.orderDate) = :year " +
            "AND MONTH(o.orderDate) = :month " +
            "ORDER BY o.orderDate DESC")
    List<SalesOrder> findMonthlySales(@Param("year") int year, @Param("month") int month);

    /**
     * Lấy recent orders
     */
    @Query("SELECT o FROM SalesOrder o " +
            "WHERE o.orderDate >= :fromDate " +
            "ORDER BY o.orderDate DESC")
    List<SalesOrder> findRecentOrders(@Param("fromDate") LocalDate fromDate);

    /**
     * Đếm orders theo status
     */
    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE " +
            "o.status = :status AND " +
            "(:salesPersonId IS NULL OR o.salesPerson.id = :salesPersonId)")
    Long countByStatus(
            @Param("status") String status,
            @Param("salesPersonId") Long salesPersonId
    );

    /**
     * Lấy pending orders của customer
     */
    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE " +
            "o.customer.id = :customerId AND " +
            "o.status NOT IN ('COMPLETED', 'CANCELLED')")
    Long countPendingByCustomer(@Param("customerId") Long customerId);

    Integer countByProductAndDateRange(Long productId, OffsetDateTime offsetDateTime, OffsetDateTime offsetDateTime1);
}
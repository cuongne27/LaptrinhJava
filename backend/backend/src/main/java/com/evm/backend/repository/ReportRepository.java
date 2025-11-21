package com.evm.backend.repository;

import com.evm.backend.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<SalesOrder, Long> {

    // =====================================================
    // D.1: SALES REPORTS - FIXED
    // =====================================================

    /**
     * Báo cáo doanh thu theo ngày
     * ✅ Fix: Không dùng constructor projection, trả về Object[] thay thế
     */
    @Query("SELECT " +
            "o.orderDate, " +
            "SUM(o.totalPrice), " +
            "COUNT(o), " +
            "AVG(o.totalPrice) " +
            "FROM SalesOrder o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND (:dealerId IS NULL OR o.vehicle.dealer.id = :dealerId) " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY o.orderDate " +
            "ORDER BY o.orderDate")
    List<Object[]> getSalesByDay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dealerId") Long dealerId
    );

    /**
     * Tổng doanh thu theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) " +
            "FROM SalesOrder o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND (:dealerId IS NULL OR o.vehicle.dealer.id = :dealerId) " +
            "AND o.status != 'CANCELLED'")
    BigDecimal getTotalRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dealerId") Long dealerId
    );

    /**
     * Tổng số đơn hàng
     */
    @Query("SELECT COUNT(o) " +
            "FROM SalesOrder o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND (:dealerId IS NULL OR o.vehicle.dealer.id = :dealerId) " +
            "AND o.status != 'CANCELLED'")
    Long getTotalOrders(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dealerId") Long dealerId
    );

    /**
     * Top sales persons
     */
    @Query("SELECT " +
            "o.salesPerson.id, " +
            "o.salesPerson.fullName, " +
            "SUM(o.totalPrice), " +
            "COUNT(o) " +
            "FROM SalesOrder o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.salesPerson IS NOT NULL " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY o.salesPerson.id, o.salesPerson.fullName " +
            "ORDER BY SUM(o.totalPrice) DESC")
    List<Object[]> getTopSalesPersons(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Top dealers
     */
    @Query("SELECT " +
            "d.id, " +
            "d.dealerName, " +
            "SUM(o.totalPrice), " +
            "COUNT(o) " +
            "FROM SalesOrder o " +
            "JOIN o.vehicle v " +
            "JOIN v.dealer d " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY d.id, d.dealerName " +
            "ORDER BY SUM(o.totalPrice) DESC")
    List<Object[]> getTopDealers(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Top products
     */
    @Query("SELECT " +
            "p.id, " +
            "p.productName, " +
            "COUNT(o), " +
            "SUM(o.totalPrice) " +
            "FROM SalesOrder o " +
            "JOIN o.vehicle v " +
            "JOIN v.product p " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY p.id, p.productName " +
            "ORDER BY COUNT(o) DESC")
    List<Object[]> getTopProducts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Total discount amount
     */
    @Query("SELECT COALESCE(SUM(o.discountAmount), 0) " +
            "FROM SalesOrder o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND (:dealerId IS NULL OR o.vehicle.dealer.id = :dealerId) " +
            "AND o.status != 'CANCELLED'")
    BigDecimal getTotalDiscount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dealerId") Long dealerId
    );

    // =====================================================
    // C.1, C.2: DEALER PERFORMANCE
    // =====================================================

    /**
     * Dealer sales by month
     */
    @Query(value = "SELECT " +
            "DATE_FORMAT(order_date, '%Y-%m') as month, " +
            "SUM(total_price) as revenue, " +
            "COUNT(*) as order_count " +
            "FROM sales_order o " +
            "JOIN vehicle v ON o.vehicle_id = v.vehicle_id " +
            "WHERE v.dealer_id = :dealerId " +
            "AND o.order_date BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY DATE_FORMAT(order_date, '%Y-%m') " +
            "ORDER BY month",
            nativeQuery = true)
    List<Object[]> getDealerSalesByMonth(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Dealer product breakdown
     */
    @Query("SELECT " +
            "p.id, " +
            "p.productName, " +
            "COUNT(o), " +
            "SUM(o.totalPrice) " +
            "FROM SalesOrder o " +
            "JOIN o.vehicle v " +
            "JOIN v.product p " +
            "WHERE v.dealer.id = :dealerId " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY p.id, p.productName " +
            "ORDER BY SUM(o.totalPrice) DESC")
    List<Object[]> getDealerProductBreakdown(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Dealer total sales
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) " +
            "FROM SalesOrder o " +
            "JOIN o.vehicle v " +
            "WHERE v.dealer.id = :dealerId " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELLED'")
    BigDecimal getDealerTotalSales(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =====================================================
    // PAYMENT REPORTS
    // =====================================================

    /**
     * Total paid amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidAmount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Orders by payment status
     */
    @Query("SELECT " +
            "o.id, " +
            "o.totalPrice, " +
            "COALESCE(SUM(p.amount), 0) " +
            "FROM SalesOrder o " +
            "LEFT JOIN o.payments p ON p.status = 'COMPLETED' " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY o.id, o.totalPrice " +
            "HAVING COALESCE(SUM(p.amount), 0) < o.totalPrice")
    List<Object[]> getOrdersWithPendingPayment(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
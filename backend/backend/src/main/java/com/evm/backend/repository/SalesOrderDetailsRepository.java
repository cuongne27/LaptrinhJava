package com.evm.backend.repository;

import com.evm.backend.entity.SalesOrder;
import com.evm.backend.entity.SalesOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SalesOrderDetailsRepository extends JpaRepository<SalesOrderDetails, Long> {
    List<SalesOrderDetails> findBySalesOrder(SalesOrder salesOrder);

    List<SalesOrderDetails> findBySalesOrderId(Long salesOrderId);

    List<SalesOrderDetails> findByItemType(String itemType);

    @Query("SELECT sod FROM SalesOrderDetails sod WHERE sod.salesOrder.id = :orderId AND sod.itemType = :itemType")
    List<SalesOrderDetails> findByOrderAndItemType(@Param("orderId") Long orderId,
                                                   @Param("itemType") String itemType);

    @Query("SELECT sod FROM SalesOrderDetails sod WHERE sod.itemName LIKE %:keyword%")
    List<SalesOrderDetails> searchByItemName(@Param("keyword") String keyword);

    @Query("SELECT SUM(sod.totalPrice) FROM SalesOrderDetails sod WHERE sod.salesOrder.id = :orderId")
    BigDecimal getTotalPriceForOrder(@Param("orderId") Long orderId);

    @Query("SELECT SUM(sod.quantity) FROM SalesOrderDetails sod WHERE sod.salesOrder.id = :orderId")
    Integer getTotalQuantityForOrder(@Param("orderId") Long orderId);

    @Query("SELECT sod FROM SalesOrderDetails sod WHERE sod.salesOrder.id = :orderId ORDER BY sod.totalPrice DESC")
    List<SalesOrderDetails> findByOrderOrderByPriceDesc(@Param("orderId") Long orderId);

    // Statistics: Most popular accessories
    @Query("SELECT sod.itemName, COUNT(sod) as count FROM SalesOrderDetails sod WHERE sod.itemType = 'ACCESSORY' GROUP BY sod.itemName ORDER BY count DESC")
    List<Object[]> findMostPopularAccessories();

    // Find all vehicle items
    @Query("SELECT sod FROM SalesOrderDetails sod WHERE sod.itemType = 'VEHICLE'")
    List<SalesOrderDetails> findAllVehicleItems();

    // Find all accessory items for a specific order
    @Query("SELECT sod FROM SalesOrderDetails sod WHERE sod.salesOrder.id = :orderId AND sod.itemType = 'ACCESSORY'")
    List<SalesOrderDetails> findAccessoriesByOrder(@Param("orderId") Long orderId);

    long countBySalesOrderId(Long salesOrderId);
}
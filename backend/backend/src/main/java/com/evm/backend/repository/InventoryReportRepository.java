package com.evm.backend.repository;

import com.evm.backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReportRepository extends JpaRepository<Inventory, Long> {

    /**
     * D.2: Inventory report with status - FIXED with Native Query
     */
    @Query(value = "SELECT " +
            "p.product_id, " +
            "p.product_name, " +
            "p.version, " +
            "COALESCE(d.dealer_id, 0), " +
            "COALESCE(d.dealer_name, 'Brand Warehouse'), " +
            "COALESCE(i.location, ''), " +
            "COALESCE(i.total_quantity, 0), " +
            "COALESCE(i.available_quantity, 0), " +
            "COALESCE(i.reserved_quantity, 0), " +
            "COALESCE(i.`in_transit_quantity`, 0), " +
            "CASE " +
            "  WHEN COALESCE(i.available_quantity, 0) = 0 THEN 'OUT_OF_STOCK' " +
            "  WHEN COALESCE(i.available_quantity, 0) < 5 THEN 'LOW_STOCK' " +
            "  ELSE 'NORMAL' " +
            "END as stock_status " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.product_id " +
            "LEFT JOIN dealer d ON i.dealer_id = d.dealer_id " +
            "WHERE (:productId IS NULL OR p.product_id = :productId) " +
            "AND (:dealerId IS NULL OR i.dealer_id = :dealerId OR (:dealerId = 0 AND i.dealer_id IS NULL)) " +
            "ORDER BY p.product_name",
            nativeQuery = true)
    List<Object[]> getInventoryReportData(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId
    );

    /**
     * Low stock alerts
     */
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.availableQuantity < 5 " +
            "AND i.availableQuantity > 0 " +
            "ORDER BY i.availableQuantity ASC")
    List<Inventory> getLowStockItems();

    /**
     * Out of stock
     */
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.availableQuantity = 0 " +
            "ORDER BY i.product.productName")
    List<Inventory> getOutOfStockItems();

    /**
     * Total statistics
     */
    @Query("SELECT " +
            "COUNT(DISTINCT i.product.id), " +
            "COALESCE(SUM(i.totalQuantity), 0), " +
            "COALESCE(SUM(i.availableQuantity), 0), " +
            "COALESCE(SUM(i.reservedQuantity), 0), " +
            "COALESCE(SUM(i.inTransitQuantity), 0) " +
            "FROM Inventory i")
    List<Object[]> getInventoryStatistics();
}
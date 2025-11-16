package com.evm.backend.repository;

import com.evm.backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Inventory entity
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    /**
     * Find inventory by product ID and dealer ID
     */
    Optional<Inventory> findByProductIdAndDealerId(Long productId, Long dealerId);

    /**
     * Find inventory by product ID, ordered by last updated
     */
    List<Inventory> findByProductIdOrderByUpdatedAtDesc(Long productId);

    /**
     * Find inventory by dealer ID, ordered by last updated
     */
    List<Inventory> findByDealerIdOrderByUpdatedAtDesc(Long dealerId);

    /**
     * Find inventory for the brand warehouse (dealer is null), ordered by last updated
     */
    List<Inventory> findByDealerIsNullOrderByUpdatedAtDesc();

    /**
     * Find inventory items where available quantity is below a given threshold
     */
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity < :threshold ORDER BY i.availableQuantity ASC")
    List<Inventory> findLowStockInventory(@Param("threshold") Integer threshold);

    /**
     * Find inventory by ID with all details (product, brand, dealer)
     */
    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN FETCH i.product p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH i.dealer " +
            "WHERE i.id = :inventoryId")
    Optional<Inventory> findByIdWithDetails(@Param("inventoryId") Long inventoryId);

    /**
     * Calculate total available quantity across all inventory records
     */
    @Query("SELECT SUM(i.availableQuantity) FROM Inventory i")
    Long getTotalAvailableQuantity();

    /**
     * Calculate total reserved quantity across all inventory records
     */
    @Query("SELECT SUM(i.reservedQuantity) FROM Inventory i")
    Long getTotalReservedQuantity();

    /**
     * Calculate total in-transit quantity across all inventory records
     */
    @Query("SELECT SUM(i.inTransitQuantity) FROM Inventory i")
    Long getTotalInTransitQuantity();

    /**
     * Count inventory records where available quantity is below a given threshold
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity < :threshold")
    Long countLowStock(@Param("threshold") Integer threshold);

    /**
     * Count inventory records belonging to the brand warehouse (dealer is null)
     */
    Long countByDealerIsNull();
}
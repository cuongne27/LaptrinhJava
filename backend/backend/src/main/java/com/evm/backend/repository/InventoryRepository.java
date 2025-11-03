package com.evm.backend.repository;

import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.Inventory;
import com.evm.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // Brand warehouse (dealer is null)
    @Query("SELECT i FROM Inventory i WHERE i.dealer IS NULL")
    List<Inventory> findBrandWarehouseInventory();

    // Dealer inventory
    List<Inventory> findByDealer(Dealer dealer);

    List<Inventory> findByDealerId(Long dealerId);

    // Find by product
    List<Inventory> findByProduct(Product product);

    List<Inventory> findByProductId(Long productId);

    // Find specific inventory
    Optional<Inventory> findByProductAndDealer(Product product, Dealer dealer);

    Optional<Inventory> findByProductIdAndDealerId(Long productId, Long dealerId);

    // Brand warehouse for specific product
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.dealer IS NULL")
    Optional<Inventory> findBrandInventoryByProduct(@Param("productId") Long productId);

    // Low stock
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= :threshold")
    List<Inventory> findLowStockInventory(@Param("threshold") Integer threshold);

    // Available inventory for dealer
    @Query("SELECT i FROM Inventory i WHERE i.dealer.id = :dealerId AND i.availableQuantity > 0")
    List<Inventory> findAvailableInventoryByDealer(@Param("dealerId") Long dealerId);

    // Total available by product
    @Query("SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalAvailableByProduct(@Param("productId") Long productId);
}
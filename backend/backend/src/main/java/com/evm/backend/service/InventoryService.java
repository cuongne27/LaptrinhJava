package com.evm.backend.service;

import com.evm.backend.dto.request.InventoryFilterRequest;
import com.evm.backend.dto.request.InventoryRequest;
import com.evm.backend.dto.response.InventoryDetailResponse;
import com.evm.backend.dto.response.InventoryListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Inventory operations
 * Quản lý tồn kho xe tại các đại lý và kho brand
 */
public interface InventoryService {

    /**
     * Get all inventory with filtering and pagination
     */
    Page<InventoryListResponse> getAllInventory(InventoryFilterRequest filterRequest);

    /**
     * Get inventory by product
     */
    List<InventoryListResponse> getInventoryByProduct(Long productId);

    /**
     * Get inventory by dealer
     */
    List<InventoryListResponse> getInventoryByDealer(Long dealerId);

    /**
     * Get brand warehouse inventory (dealer = null)
     */
    List<InventoryListResponse> getBrandWarehouseInventory();

    /**
     * Get low stock inventory (available < threshold)
     */
    List<InventoryListResponse> getLowStockInventory(Integer threshold);

    /**
     * Get inventory detail by ID
     */
    InventoryDetailResponse getInventoryById(Long inventoryId);

    /**
     * Get inventory by product and dealer
     */
    InventoryDetailResponse getInventoryByProductAndDealer(Long productId, Long dealerId);

    /**
     * Create new inventory
     */
    InventoryDetailResponse createInventory(InventoryRequest request);

    /**
     * Update inventory
     */
    InventoryDetailResponse updateInventory(Long inventoryId, InventoryRequest request);

    /**
     * Adjust inventory quantity (increase/decrease)
     */
    InventoryDetailResponse adjustQuantity(Long inventoryId, Integer quantity, String reason);

    /**
     * Reserve inventory (for order)
     */
    InventoryDetailResponse reserveInventory(Long inventoryId, Integer quantity);

    /**
     * Release reserved inventory (cancel order)
     */
    InventoryDetailResponse releaseReservedInventory(Long inventoryId, Integer quantity);

    /**
     * Transfer inventory (from brand warehouse to dealer)
     */
    InventoryDetailResponse transferInventory(Long fromInventoryId, Long toDealerId, Integer quantity);

    /**
     * Delete inventory
     */
    void deleteInventory(Long inventoryId);

    /**
     * Get inventory summary statistics
     */
    java.util.Map<String, Object> getInventoryStatistics();
}
package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Inventory List
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryListResponse {
    private Long inventoryId;

    // Product info
    private Long productId;
    private String productName;
    private String productVersion;
    private String brandName;

    // Dealer info (null = brand warehouse)
    private Long dealerId;
    private String dealerName;
    private String dealerLocation;

    // Quantities
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer inTransitQuantity;

    private String location;
    private OffsetDateTime updatedAt;

    // Calculated
    private Double stockPercentage; // available/total * 100
    private Boolean isLowStock; // available < threshold
}
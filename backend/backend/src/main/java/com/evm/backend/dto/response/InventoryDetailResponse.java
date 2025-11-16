package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Inventory Detail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDetailResponse {
    private Long inventoryId;

    // Product info
    private Long productId;
    private String productName;
    private String productVersion;
    private String productDescription;
    private java.math.BigDecimal productPrice;

    // Brand info
    private Integer brandId;
    private String brandName;
    private String brandContactInfo;

    // Dealer info (null = brand warehouse)
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;
    private String dealerPhone;
    private String dealerEmail;

    // Quantities
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer inTransitQuantity;

    private String location;
    private OffsetDateTime updatedAt;

    // Statistics
    private Double stockPercentage;
    private Boolean isLowStock;
    private Integer soldQuantity; // calculated: initial total - current total
}
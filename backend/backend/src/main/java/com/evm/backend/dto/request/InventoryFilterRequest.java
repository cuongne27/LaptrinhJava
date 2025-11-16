package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering inventory
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryFilterRequest {

    private String searchKeyword; // Search in product name, location
    private Long productId;
    private Long dealerId;
    private Integer brandId;
    private Boolean isBrandWarehouse; // true = dealer is null
    private Integer minAvailable; // Filter available >= minAvailable
    private Integer maxAvailable; // Filter available <= maxAvailable

    // Sorting: product_asc, product_desc, available_asc, available_desc, updated_asc, updated_desc
    private String sortBy;

    private Integer page;
    private Integer size;
}
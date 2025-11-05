package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for filtering and searching products
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilterRequest {
    private String searchKeyword;
    private Long brandId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // price_asc, price_desc, name_asc, name_desc
    private Integer page;
    private Integer size;
}
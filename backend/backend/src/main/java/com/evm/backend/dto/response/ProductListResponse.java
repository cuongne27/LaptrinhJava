package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dùng cho danh sách sản phẩm
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponse {
    private Long id;
    private String productName;
    private String version;
    private BigDecimal msrp;
    private String imageUrl;
    private String brandName;
    private Long brandId;
    private Boolean isActive;

    // Available colors at dealer
    private List<String> availableColors;

    // Available quantity at dealer
    private Long availableQuantity;
}
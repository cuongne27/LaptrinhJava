package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dùng cho chi tiết sản phẩm
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String productName;
    private String version;
    private BigDecimal msrp;
    private String specifications;
    private String description;
    private String brandName;
    private Long brandId;
    private Boolean isActive;

    // Media
    private String imageUrl;
    private String videoUrl;

    // Variants (colors with quantity)
    private List<ProductVariantResponse> variants;

    // Features
    private List<ProductFeatureResponse> features;

    // Technical specs
    private TechnicalSpecsResponse technicalSpecs;
}
package com.evm.backend.dto.request;

import com.evm.backend.entity.ProductFeature;
import com.evm.backend.entity.ProductVariant;
import com.evm.backend.entity.TechnicalSpecs;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unified Request DTO for Product (dùng chung cho CREATE và UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm không được vượt quá 150 ký tự")
    private String productName;

    @Size(max = 50, message = "Phiên bản không được vượt quá 50 ký tự")
    private String version;

    @NotNull(message = "Giá bán lẻ đề xuất không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal msrp;

    private String description;

    @Size(max = 255, message = "URL hình ảnh không được vượt quá 255 ký tự")
    private String imageUrl;

    @Size(max = 255, message = "URL video không được vượt quá 255 ký tự")
    private String videoUrl;

    @NotNull(message = "Brand ID không được để trống")
    private int brandId;

    private Boolean isActive;

    // Technical Specs (Embedded)
    @Valid
    private TechnicalSpecs technicalSpecs;

    // Features
    @Valid
    private List<ProductFeatureRequest> features;

    // Variants (màu sắc)
    @Valid
    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 màu sắc")
    private List<ProductVariantRequest> variants;
}
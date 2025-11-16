package com.evm.backend.controller;

import com.evm.backend.dto.response.ProductComparisonResponse;
import com.evm.backend.service.ProductComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-comparisons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Comparison", description = "APIs so sánh mẫu xe điện")
public class ProductComparisonController {

    private final ProductComparisonService productComparisonService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "So sánh nhiều mẫu xe",
            description = "So sánh 2-3 mẫu xe điện về thông số kỹ thuật và giá")
    public ResponseEntity<ProductComparisonResponse> compareProducts(
            @Parameter(description = "Danh sách ID sản phẩm (2-3 sản phẩm)", required = true)
            @RequestParam List<Long> productIds
    ) {
        log.info("REST request to compare products: {}", productIds);
        ProductComparisonResponse response = productComparisonService.compareProducts(productIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-criteria")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "So sánh theo tiêu chí",
            description = "So sánh sản phẩm và sắp xếp theo tiêu chí cụ thể")
    public ResponseEntity<ProductComparisonResponse> compareProductsByCriteria(
            @Parameter(description = "Danh sách ID sản phẩm")
            @RequestParam List<Long> productIds,
            @Parameter(description = "Tiêu chí: RANGE, POWER, BATTERY, PRICE, CHARGING_TIME")
            @RequestParam String criteria
    ) {
        log.info("REST request to compare products by criteria: {} - {}", productIds, criteria);
        ProductComparisonResponse response = productComparisonService.compareProductsByCriteria(
                productIds, criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-recommendation")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "So sánh với khuyến nghị",
            description = "So sánh và đề xuất sản phẩm phù hợp theo nhu cầu")
    public ResponseEntity<ProductComparisonResponse> compareWithRecommendation(
            @Parameter(description = "Danh sách ID sản phẩm")
            @RequestParam List<Long> productIds,
            @Parameter(description = "Nhu cầu: CITY, LONG_DISTANCE, BUDGET, PERFORMANCE")
            @RequestParam String userNeeds
    ) {
        log.info("REST request to compare products with recommendation: {} - {}",
                productIds, userNeeds);
        ProductComparisonResponse response = productComparisonService.compareWithRecommendation(
                productIds, userNeeds);
        return ResponseEntity.ok(response);
    }
}
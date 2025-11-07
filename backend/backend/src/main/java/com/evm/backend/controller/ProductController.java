package com.evm.backend.controller;

import com.evm.backend.dto.request.ProductFilterRequest;
import com.evm.backend.dto.response.ProductDetailResponse;
import com.evm.backend.dto.response.ProductListResponse;
import com.evm.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for Product Catalog operations
 * UC-DL-01: Xem danh mục và thông tin chi tiết xe
 */
@RestController
@RequestMapping("/api/dealer/products")
@RequiredArgsConstructor
@Tag(name = "Dealer Product Catalog", description = "APIs for dealer staff to view product catalog")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    /**
     * UC-DL-01: Xem danh mục xe với filter và search
     * GET /api/dealer/products
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'ADMIN')")
    @Operation(
            summary = "Get product catalog",
            description = "View all available electric vehicles with filtering and search capabilities"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product catalog"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have DEALER_STAFF or ADMIN role"),
            @ApiResponse(responseCode = "400", description = "Bad request - User not associated with dealer")
    })
    public ResponseEntity<Page<ProductListResponse>> getProductCatalog(
            Authentication authentication,

            @Parameter(description = "Search keyword for product name")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by brand ID")
            @RequestParam(required = false) Long brandId,

            @Parameter(description = "Minimum price")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Sort by: price_asc, price_desc, name_asc, name_desc")
            @RequestParam(required = false, defaultValue = "name_asc") String sortBy,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Page size (max 100)")
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        String username = authentication.getName();

        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .searchKeyword(search)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<ProductListResponse> products = productService.getProductCatalog(username, filterRequest);

        return ResponseEntity.ok(products);
    }

    /**
     * UC-DL-01: Xem chi tiết sản phẩm
     * GET /api/dealer/products/{productId}
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'ADMIN')")
    @Operation(
            summary = "Get product details",
            description = "View detailed information about a specific electric vehicle including specs, features, and available variants"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have DEALER_STAFF or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Not found - Product does not exist"),
            @ApiResponse(responseCode = "400", description = "Bad request - User not associated with dealer")
    })
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            Authentication authentication,

            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    ) {
        String username = authentication.getName();

        ProductDetailResponse productDetail = productService.getProductDetail(username, productId);

        return ResponseEntity.ok(productDetail);
    }

    /**
     * Alternative endpoint: Search products using POST method
     * POST /api/dealer/products/search
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'ADMIN')")
    @Operation(
            summary = "Search products",
            description = "Search and filter products using POST method with request body"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product catalog"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have DEALER_STAFF or ADMIN role"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid filter parameters or user not associated with dealer")
    })
    public ResponseEntity<Page<ProductListResponse>> searchProducts(
            Authentication authentication,

            @Parameter(description = "Filter request body", required = true)
            @RequestBody ProductFilterRequest filterRequest
    ) {
        String username = authentication.getName();

        Page<ProductListResponse> products = productService.getProductCatalog(username, filterRequest);

        return ResponseEntity.ok(products);
    }
}
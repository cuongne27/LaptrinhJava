package com.evm.backend.controller;

import com.evm.backend.dto.request.ProductRequest;
import com.evm.backend.dto.response.ProductDetailResponse;
import com.evm.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Product CRUD operations
 * Chỉ BRAND_MANAGER có quyền thực hiện CRUD
 */
@RestController
@RequestMapping("/api/brand/products")
@RequiredArgsConstructor
@Tag(name = "Brand Product Management", description = "APIs for managing products (CRUD)")
@SecurityRequirement(name = "bearerAuth")
public class ProductCrudController {

    private final ProductService productService;

    /**
     * CREATE - Tạo sản phẩm mới
     * POST /api/brand/products
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create new product",
            description = "Create a new electric vehicle product with full details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have BRAND_MANAGER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Brand not found")
    })
    public ResponseEntity<ProductDetailResponse> createProduct(
            Authentication authentication,
            @Valid @RequestBody ProductRequest request
    ) {
        String username = authentication.getName();

        ProductDetailResponse response = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * UPDATE - Cập nhật sản phẩm
     * PUT /api/brand/products/{productId}
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Update product",
            description = "Update an existing product with full details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have BRAND_MANAGER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Product or Brand not found")
    })
    public ResponseEntity<ProductDetailResponse> updateProduct(
            Authentication authentication,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
        String username = authentication.getName();

        ProductDetailResponse response = productService.updateProduct(productId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * SOFT DELETE - Đặt sản phẩm thành inactive
     * DELETE /api/brand/products/{productId}
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Delete product (soft delete)",
            description = "Deactivate a product by setting isActive to false"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have BRAND_MANAGER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            Authentication authentication,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    ) {
        String username = authentication.getName();

        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }

    /**
     * HARD DELETE - Xóa sản phẩm vĩnh viễn
     * DELETE /api/brand/products/{productId}/permanent
     */
    @DeleteMapping("/{productId}/permanent")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Hard delete product",
            description = "Permanently remove a product from database (DANGEROUS!)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product permanently deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have BRAND_MANAGER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> hardDeleteProduct(
            Authentication authentication,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    ) {
        String username = authentication.getName();

        productService.hardDeleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}
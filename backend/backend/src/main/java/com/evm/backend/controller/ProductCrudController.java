package com.evm.backend.controller;

import com.evm.backend.dto.request.ProductRequest;
import com.evm.backend.dto.response.ProductDetailResponse;
import com.evm.backend.dto.response.ProductListResponse;
import com.evm.backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * Chỉ BRAND_MANAGER và ADMIN có quyền thực hiện CRUD
 */
@RestController
@RequestMapping("/api/products") // <<< MODULE: QUẢN LÝ SẢN PHẨM (PRODUCT CRUD)
@RequiredArgsConstructor
@Tag(name = "Brand Product Management", description = "APIs for managing products (CRUD)")
@SecurityRequirement(name = "bearerAuth")
public class ProductCrudController {

    private final ProductService productService;

    /**
     * GET ALL - Lấy danh sách tất cả sản phẩm (có phân trang)
     * GET /api/products
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH TẤT CẢ SẢN PHẨM (CÓ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/products
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get all products",
            description = "Get all products with pagination and search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have BRAND_MANAGER or ADMIN role")
    })
    public ResponseEntity<Page<ProductListResponse>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Search keyword for product name")
            @RequestParam(required = false) String searchKeyword,

            @Parameter(description = "Sort by field (id, productName, msrp)")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (asc, desc)")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ProductListResponse> products;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            products = productService.getAllProducts(searchKeyword.trim(), pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        return ResponseEntity.ok(products);
    }

    /**
     * GET BY ID - Lấy chi tiết sản phẩm
     * GET /api/products/{productId}
     */
    // <<< CHỨC NĂNG: XEM CHI TIẾT SẢN PHẨM
    // <<< ĐẦU API: GET /api/products/{productId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get product by ID",
            description = "Get detailed information about a specific product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have BRAND_MANAGER or ADMIN role")
    })
    public ResponseEntity<ProductDetailResponse> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    ) {
        ProductDetailResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * CREATE - Tạo sản phẩm mới
     * POST /api/products/create
     */
    // <<< CHỨC NĂNG: TẠO SẢN PHẨM MỚI
    // <<< ĐẦU API: POST /api/products/create
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
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
     * PUT /api/products/{productId}
     */
    // <<< CHỨC NĂNG: CẬP NHẬT THÔNG TIN SẢN PHẨM
    // <<< ĐẦU API: PUT /api/products/{productId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
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
     * DELETE /api/products/{productId}
     */
    // <<< CHỨC NĂNG: SOFT DELETE (VÔ HIỆU HÓA) SẢN PHẨM
    // <<< ĐẦU API: DELETE /api/products/{productId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
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
     * DELETE /api/products/{productId}/permanent
     */
    // <<< CHỨC NĂNG: HARD DELETE (XÓA VĨNH VIỄN) SẢN PHẨM
    // <<< ĐẦU API: DELETE /api/products/{productId}/permanent
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
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
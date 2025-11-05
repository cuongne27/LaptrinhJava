package com.evm.backend.controller;

import com.evm.backend.dto.request.ProductFilterRequest;
import com.evm.backend.dto.request.ProductRequest;
import com.evm.backend.dto.response.ProductDetailResponse;
import com.evm.backend.dto.response.ProductListResponse;
import com.evm.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller cho Product Management
 * UC-DL-01: Xem danh mục và thông tin chi tiết xe
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs quản lý sản phẩm xe điện")
public class ProductController {

    private final ProductService productService;

    /**
     * GET: Lấy danh sách sản phẩm với filter và phân trang
     * Endpoint: GET /api/products
     */
    @GetMapping
    @Operation(
            summary = "Lấy danh sách sản phẩm",
            description = "Lấy danh sách sản phẩm có sẵn tại dealer với filter và phân trang"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<ProductListResponse>> getProductCatalog(
            Authentication authentication,
            @Parameter(description = "Brand ID để filter")
            @RequestParam(required = false) Long brandId,

            @Parameter(description = "Từ khóa tìm kiếm (tên sản phẩm)")
            @RequestParam(required = false) String searchKeyword,

            @Parameter(description = "Giá tối thiểu")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Giá tối đa")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Sắp xếp: price_asc, price_desc, name_asc, name_desc")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Số lượng items mỗi trang (max 100)")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/products - User: {}, Filters: brandId={}, search={}, minPrice={}, maxPrice={}",
                getUsername(authentication), brandId, searchKeyword, minPrice, maxPrice);

        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .brandId(brandId)
                .searchKeyword(searchKeyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<ProductListResponse> products = productService.getProductCatalog(
                getUsername(authentication),
                filterRequest
        );

        return ResponseEntity.ok(products);
    }

    /**
     * UC-DL-01: Xem chi tiết sản phẩm
     * GET /api/dealer/products/{productId}
     */
    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product details",
            description = "View detailed information about a specific electric vehicle including specs, features, and available variants"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have DEALER_STAFF role"),
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
     * POST: Tạo sản phẩm mới
     * Endpoint: POST /api/products
     */
    @PostMapping
    @Operation(
            summary = "Tạo sản phẩm mới",
            description = "Tạo một sản phẩm mới với thông tin chi tiết, thông số kỹ thuật, tính năng và variants"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo sản phẩm thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Brand không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền tạo sản phẩm")
    })
    public ResponseEntity<ProductDetailResponse> createProduct(
            Authentication authentication,
            @Parameter(description = "Thông tin sản phẩm cần tạo", required = true)
            @Valid @RequestBody ProductRequest productRequest
    ) {
        log.info("POST /api/products - User: {}, Product: {}",
                getUsername(authentication), productRequest.getProductName());

        ProductDetailResponse createdProduct = productService.createProduct(productRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Helper method: Lấy username từ Authentication
     */
    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        return authentication.getPrincipal().toString();
    }
}
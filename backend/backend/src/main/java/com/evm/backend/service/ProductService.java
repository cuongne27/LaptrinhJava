package com.evm.backend.service;

import com.evm.backend.dto.request.ProductFilterRequest;
import com.evm.backend.dto.request.ProductRequest;
import com.evm.backend.dto.response.ProductDetailResponse;
import com.evm.backend.dto.response.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for Product operations
 * UC-DL-01: Xem danh mục và thông tin chi tiết xe
 */
public interface ProductService {

    /**
     * Get all products with pagination (for CRUD management)
     *
     * @param pageable Pagination parameters
     * @return Page of ProductListResponse
     */
    Page<ProductListResponse> getAllProducts(Pageable pageable);

    /**
     * Get all products with pagination and search (for CRUD management)
     *
     * @param searchKeyword Search keyword for product name
     * @param pageable Pagination parameters
     * @return Page of ProductListResponse
     */
    Page<ProductListResponse> getAllProducts(String searchKeyword, Pageable pageable);

    /**
     * Get product catalog with filtering and pagination
     *
     * @param username Username of the dealer staff
     * @param filterRequest Filter and pagination parameters
     * @return Page of ProductListResponse
     */
    Page<ProductListResponse> getProductCatalog(String username, ProductFilterRequest filterRequest);

    /**
     * Get detailed information of a specific product
     *
     * @param username Username of the dealer staff
     * @param productId Product ID
     * @return ProductDetailResponse with full product information
     */
    ProductDetailResponse getProductDetail(String username, Long productId);

    /**
     * Get product detail by ID (for CRUD management, no dealer context)
     *
     * @param productId Product ID
     * @return ProductDetailResponse with full product information
     */
    ProductDetailResponse getProductById(Long productId);

    ProductDetailResponse createProduct(ProductRequest productRequest);

    @Transactional
    ProductDetailResponse updateProduct(Long productId, ProductRequest productRequest);

    @Transactional
    void deleteProduct(Long productId);

    @Transactional
    void hardDeleteProduct(Long productId);
}
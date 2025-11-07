package com.evm.backend.service;

import com.evm.backend.dto.request.ProductFilterRequest;
import com.evm.backend.dto.request.ProductRequest;
import com.evm.backend.dto.response.ProductDetailResponse;
import com.evm.backend.dto.response.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for Product operations
 * UC-DL-01: Xem danh mục và thông tin chi tiết xe
 */
public interface ProductService {

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

    ProductDetailResponse createProduct(ProductRequest productRequest);

    @Transactional
    ProductDetailResponse updateProduct(Long productId, ProductRequest productRequest);

    @Transactional
    void deleteProduct(Long productId);

    @Transactional
    void hardDeleteProduct(Long productId);
}
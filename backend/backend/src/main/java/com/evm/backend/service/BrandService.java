package com.evm.backend.service;

import com.evm.backend.dto.request.BrandRequest;
import com.evm.backend.dto.response.BrandDetailResponse;
import com.evm.backend.dto.response.BrandListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Brand operations
 * CRUD operations for Brand management
 */
public interface BrandService {

    /**
     * Get all brands with pagination
     *
     * @param pageable Pagination parameters
     * @return Page of BrandListResponse
     */
    Page<BrandListResponse> getAllBrands(Pageable pageable);

    /**
     * Get all brands without pagination
     *
     * @return List of BrandListResponse
     */
    List<BrandListResponse> getAllBrands();

    /**
     * Get brand by ID
     *
     * @param brandId Brand ID
     * @return BrandDetailResponse
     */
    BrandDetailResponse getBrandById(Integer brandId);

    /**
     * Create a new brand
     *
     * @param request Brand request
     * @return BrandDetailResponse of created brand
     */
    BrandDetailResponse createBrand(BrandRequest request);

    /**
     * Update an existing brand
     *
     * @param brandId Brand ID to update
     * @param request Brand request with updated data
     * @return BrandDetailResponse of updated brand
     */
    BrandDetailResponse updateBrand(Integer brandId, BrandRequest request);

    /**
     * Delete a brand
     *
     * @param brandId Brand ID to delete
     */
    void deleteBrand(Integer brandId);
}
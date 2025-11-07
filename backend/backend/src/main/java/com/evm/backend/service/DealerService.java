package com.evm.backend.service;

import com.evm.backend.dto.request.DealerFilterRequest;
import com.evm.backend.dto.request.DealerRequest;
import com.evm.backend.dto.response.DealerDetailResponse;
import com.evm.backend.dto.response.DealerListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Dealer operations
 * CRUD operations for Dealer management
 */
public interface DealerService {

    /**
     * Get all dealers with filtering and pagination
     *
     * @param filterRequest Filter parameters
     * @return Page of DealerListResponse
     */
    Page<DealerListResponse> getAllDealers(DealerFilterRequest filterRequest);

    /**
     * Get all dealers of a specific brand
     *
     * @param brandId Brand ID
     * @return List of DealerListResponse
     */
    List<DealerListResponse> getDealersByBrand(Integer brandId);

    /**
     * Get dealer by ID
     *
     * @param dealerId Dealer ID
     * @return DealerDetailResponse
     */
    DealerDetailResponse getDealerById(Long dealerId);

    /**
     * Create a new dealer
     *
     * @param request Dealer request
     * @return DealerDetailResponse of created dealer
     */
    DealerDetailResponse createDealer(DealerRequest request);

    /**
     * Update an existing dealer
     *
     * @param dealerId Dealer ID to update
     * @param request Dealer request with updated data
     * @return DealerDetailResponse of updated dealer
     */
    DealerDetailResponse updateDealer(Long dealerId, DealerRequest request);

    /**
     * Delete a dealer
     *
     * @param dealerId Dealer ID to delete
     */
    void deleteDealer(Long dealerId);
}
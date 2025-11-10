package com.evm.backend.service;

import com.evm.backend.dto.request.DealerContractFilterRequest;
import com.evm.backend.dto.request.DealerContractRequest;
import com.evm.backend.dto.response.DealerContractDetailResponse;
import com.evm.backend.dto.response.DealerContractListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for DealerContract operations
 * CRUD operations for DealerContract management
 */
public interface DealerContractService {

    /**
     * Get all contracts with filtering and pagination
     *
     * @param filterRequest Filter parameters
     * @return Page of DealerContractListResponse
     */
    Page<DealerContractListResponse> getAllContracts(DealerContractFilterRequest filterRequest);

    /**
     * Get all contracts of a specific dealer
     *
     * @param dealerId Dealer ID
     * @return List of DealerContractListResponse
     */
    List<DealerContractListResponse> getContractsByDealer(Long dealerId);

    /**
     * Get all contracts of a specific brand
     *
     * @param brandId Brand ID
     * @return List of DealerContractListResponse
     */
    List<DealerContractListResponse> getContractsByBrand(Integer brandId);

    /**
     * Get contract by ID
     *
     * @param contractId Contract ID
     * @return DealerContractDetailResponse
     */
    DealerContractDetailResponse getContractById(Long contractId);

    /**
     * Create a new contract
     *
     * @param request Contract request
     * @return DealerContractDetailResponse of created contract
     */
    DealerContractDetailResponse createContract(DealerContractRequest request);

    /**
     * Update an existing contract
     *
     * @param contractId Contract ID to update
     * @param request Contract request with updated data
     * @return DealerContractDetailResponse of updated contract
     */
    DealerContractDetailResponse updateContract(Long contractId, DealerContractRequest request);

    /**
     * Delete a contract
     *
     * @param contractId Contract ID to delete
     */
    void deleteContract(Long contractId);
}
package com.evm.backend.service;

import com.evm.backend.dto.request.VehicleFilterRequest;
import com.evm.backend.dto.request.VehicleRequest;
import com.evm.backend.dto.response.VehicleDetailResponse;
import com.evm.backend.dto.response.VehicleListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Vehicle operations
 * CRUD operations for Vehicle management
 */
public interface VehicleService {

    /**
     * Get all vehicles with filtering and pagination
     *
     * @param filterRequest Filter parameters
     * @return Page of VehicleListResponse
     */
    Page<VehicleListResponse> getAllVehicles(VehicleFilterRequest filterRequest);

    /**
     * Get all vehicles of a specific product
     *
     * @param productId Product ID
     * @return List of VehicleListResponse
     */
    List<VehicleListResponse> getVehiclesByProduct(Long productId);

    /**
     * Get all vehicles of a specific dealer
     *
     * @param dealerId Dealer ID
     * @return List of VehicleListResponse
     */
    List<VehicleListResponse> getVehiclesByDealer(Long dealerId);

    /**
     * Get available vehicles of a dealer
     *
     * @param dealerId Dealer ID
     * @return List of VehicleListResponse
     */
    List<VehicleListResponse> getAvailableVehiclesByDealer(Long dealerId);

    /**
     * Get vehicle by ID
     *
     * @param vehicleId Vehicle ID
     * @return VehicleDetailResponse
     */
    VehicleDetailResponse getVehicleById(String vehicleId);

    /**
     * Get vehicle by VIN
     *
     * @param vin VIN
     * @return VehicleDetailResponse
     */
    VehicleDetailResponse getVehicleByVin(String vin);

    /**
     * Create a new vehicle
     *
     * @param request Vehicle request
     * @return VehicleDetailResponse of created vehicle
     */
    VehicleDetailResponse createVehicle(VehicleRequest request);

    /**
     * Update an existing vehicle
     *
     * @param vehicleId Vehicle ID to update
     * @param request Vehicle request with updated data
     * @return VehicleDetailResponse of updated vehicle
     */
    VehicleDetailResponse updateVehicle(String vehicleId, VehicleRequest request);

    /**
     * Delete a vehicle
     *
     * @param vehicleId Vehicle ID to delete
     */
    void deleteVehicle(String vehicleId);
}
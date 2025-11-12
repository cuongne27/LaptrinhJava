package com.evm.backend.service.impl;

import com.evm.backend.dto.request.VehicleFilterRequest;
import com.evm.backend.dto.request.VehicleRequest;
import com.evm.backend.dto.response.VehicleDetailResponse;
import com.evm.backend.dto.response.VehicleListResponse;
import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.Product;
import com.evm.backend.entity.Vehicle;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.DealerRepository;
import com.evm.backend.repository.ProductRepository;
import com.evm.backend.repository.VehicleRepository;
import com.evm.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ProductRepository productRepository;
    private final DealerRepository dealerRepository;

    @Override
    public Page<VehicleListResponse> getAllVehicles(VehicleFilterRequest filterRequest) {
        log.debug("Getting all vehicles with filters: {}", filterRequest);

        Pageable pageable = buildPageable(filterRequest);

        Page<Vehicle> vehiclesPage = vehicleRepository.findVehiclesWithFilters(
                filterRequest.getProductId(),
                filterRequest.getDealerId(),
                filterRequest.getColor(),
                filterRequest.getStatus(),
                filterRequest.getManufactureFromDate(),
                filterRequest.getManufactureToDate(),
                filterRequest.getSearchKeyword(),
                pageable
        );

        return vehiclesPage.map(this::convertToListResponse);
    }

    @Override
    public List<VehicleListResponse> getVehiclesByProduct(Long productId) {
        log.debug("Getting vehicles by productId: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<Vehicle> vehicles = vehicleRepository.findByProductId(productId);

        return vehicles.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleListResponse> getVehiclesByDealer(Long dealerId) {
        log.debug("Getting vehicles by dealerId: {}", dealerId);

        if (!dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException("Dealer not found with id: " + dealerId);
        }

        List<Vehicle> vehicles = vehicleRepository.findByDealerId(dealerId);

        return vehicles.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleListResponse> getAvailableVehiclesByDealer(Long dealerId) {
        log.debug("Getting available vehicles by dealerId: {}", dealerId);

        if (!dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException("Dealer not found with id: " + dealerId);
        }

        List<Vehicle> vehicles = vehicleRepository.findAvailableVehiclesByDealer(dealerId);

        return vehicles.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleDetailResponse getVehicleById(String vehicleId) {
        log.debug("Getting vehicle by id: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with id: " + vehicleId));

        return convertToDetailResponse(vehicle);
    }

    @Override
    public VehicleDetailResponse getVehicleByVin(String vin) {
        log.debug("Getting vehicle by VIN: {}", vin);

        Vehicle vehicle = vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with VIN: " + vin));

        return convertToDetailResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleDetailResponse createVehicle(VehicleRequest request) {
        log.info("Creating vehicle: {}", request.getId());

        // Check if vehicle ID already exists
        if (vehicleRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Vehicle ID already exists: " + request.getId());
        }

        // Check if VIN already exists
        if (vehicleRepository.existsByVin(request.getVin())) {
            throw new IllegalArgumentException("VIN already exists: " + request.getVin());
        }

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        // Validate dealer exists
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + request.getDealerId()));

        // Create vehicle
        Vehicle vehicle = Vehicle.builder()
                .id(request.getId())
                .vin(request.getVin())
                .batterySerial(request.getBatterySerial())
                .color(request.getColor())
                .manufactureDate(request.getManufactureDate())
                .status(request.getStatus() != null ? request.getStatus() : "AVAILABLE")
                .product(product)
                .dealer(dealer)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle created successfully: {}", savedVehicle.getId());

        return convertToDetailResponse(savedVehicle);
    }

    @Override
    @Transactional
    public VehicleDetailResponse updateVehicle(String vehicleId, VehicleRequest request) {
        log.info("Updating vehicle: {}", vehicleId);

        // Find existing vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with id: " + vehicleId));

        // Check VIN uniqueness (if changed)
        if (!vehicle.getVin().equals(request.getVin())) {
            if (vehicleRepository.existsByVin(request.getVin())) {
                throw new IllegalArgumentException("VIN already exists: " + request.getVin());
            }
        }

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        // Validate dealer exists
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + request.getDealerId()));

        // Update fields (Note: cannot update ID)
        vehicle.setVin(request.getVin());
        vehicle.setBatterySerial(request.getBatterySerial());
        vehicle.setColor(request.getColor());
        vehicle.setManufactureDate(request.getManufactureDate());
        vehicle.setStatus(request.getStatus());
        vehicle.setProduct(product);
        vehicle.setDealer(dealer);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        log.info("Vehicle updated successfully: {}", vehicleId);

        return convertToDetailResponse(updatedVehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(String vehicleId) {
        log.info("Deleting vehicle: {}", vehicleId);

        // Check if vehicle exists
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + vehicleId);
        }

        // Check if vehicle has sales order
        if (vehicleRepository.hasSalesOrder(vehicleId)) {
            throw new IllegalStateException(
                    "Cannot delete vehicle. It has an associated sales order");
        }

        // Delete vehicle
        vehicleRepository.deleteById(vehicleId);

        log.info("Vehicle deleted successfully: {}", vehicleId);
    }

    private VehicleListResponse convertToListResponse(Vehicle vehicle) {
        Boolean hasSalesOrder = vehicleRepository.hasSalesOrder(vehicle.getId());
        Integer totalTickets = vehicleRepository.countSupportTicketsByVehicleId(vehicle.getId()).intValue();

        return VehicleListResponse.builder()
                .id(vehicle.getId())
                .vin(vehicle.getVin())
                .color(vehicle.getColor())
                .manufactureDate(vehicle.getManufactureDate())
                .status(vehicle.getStatus())
                .productId(vehicle.getProduct() != null ? vehicle.getProduct().getId() : null)
                .productName(vehicle.getProduct() != null ? vehicle.getProduct().getProductName() : null)
                .productVersion(vehicle.getProduct() != null ? vehicle.getProduct().getVersion() : null)
                .dealerId(vehicle.getDealer() != null ? vehicle.getDealer().getId() : null)
                .dealerName(vehicle.getDealer() != null ? vehicle.getDealer().getDealerName() : null)
                .hasSalesOrder(hasSalesOrder)
                .totalSupportTickets(totalTickets)
                .build();
    }

    private VehicleDetailResponse convertToDetailResponse(Vehicle vehicle) {
        Integer totalTickets = vehicleRepository.countSupportTicketsByVehicleId(vehicle.getId()).intValue();
        Integer openTickets = vehicleRepository.countOpenTicketsByVehicleId(vehicle.getId()).intValue();
        Integer closedTickets = vehicleRepository.countClosedTicketsByVehicleId(vehicle.getId()).intValue();

        // Get sales order info if exists (simplified - actual implementation depends on SalesOrder entity)
        VehicleDetailResponse.VehicleDetailResponseBuilder builder = VehicleDetailResponse.builder()
                .id(vehicle.getId())
                .vin(vehicle.getVin())
                .batterySerial(vehicle.getBatterySerial())
                .color(vehicle.getColor())
                .manufactureDate(vehicle.getManufactureDate())
                .status(vehicle.getStatus())
                .productId(vehicle.getProduct() != null ? vehicle.getProduct().getId() : null)
                .productName(vehicle.getProduct() != null ? vehicle.getProduct().getProductName() : null)
                .productVersion(vehicle.getProduct() != null ? vehicle.getProduct().getVersion() : null)
                .productImageUrl(vehicle.getProduct() != null ? vehicle.getProduct().getImageUrl() : null)
                .dealerId(vehicle.getDealer() != null ? vehicle.getDealer().getId() : null)
                .dealerName(vehicle.getDealer() != null ? vehicle.getDealer().getDealerName() : null)
                .dealerAddress(vehicle.getDealer() != null ? vehicle.getDealer().getAddress() : null)
                .totalSupportTickets(totalTickets)
                .openTickets(openTickets)
                .closedTickets(closedTickets);

        return builder.build();
    }

    private Pageable buildPageable(VehicleFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;

        if (size > 100) size = 100;

        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null) {
            switch (filterRequest.getSortBy()) {
                case "date_asc": sort = Sort.by(Sort.Direction.ASC, "manufactureDate"); break;
                case "date_desc": sort = Sort.by(Sort.Direction.DESC, "manufactureDate"); break;
                case "status_asc": sort = Sort.by(Sort.Direction.ASC, "status"); break;
                case "status_desc": sort = Sort.by(Sort.Direction.DESC, "status"); break;
                default: sort = Sort.by(Sort.Direction.DESC, "manufactureDate");
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
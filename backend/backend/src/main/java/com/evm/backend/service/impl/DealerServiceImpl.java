package com.evm.backend.service.impl;

import com.evm.backend.dto.request.DealerFilterRequest;
import com.evm.backend.dto.request.DealerRequest;
import com.evm.backend.dto.response.DealerDetailResponse;
import com.evm.backend.dto.response.DealerListResponse;
import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Dealer;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.BrandRepository;
import com.evm.backend.repository.DealerRepository;
import com.evm.backend.service.DealerService;
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

/**
 * Implementation of DealerService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DealerServiceImpl implements DealerService {

    private final DealerRepository dealerRepository;
    private final BrandRepository brandRepository;

    @Override
    public Page<DealerListResponse> getAllDealers(DealerFilterRequest filterRequest) {
        log.debug("Getting all dealers with filters: {}", filterRequest);

        Pageable pageable = buildPageable(filterRequest);

        Page<Dealer> dealersPage = dealerRepository.findDealersWithFilters(
                filterRequest.getBrandId(),
                filterRequest.getSearchKeyword(),
                filterRequest.getDealerLevel(),
                pageable
        );

        return dealersPage.map(this::convertToListResponse);
    }

    @Override
    public List<DealerListResponse> getDealersByBrand(Integer brandId) {
        log.debug("Getting dealers by brandId: {}", brandId);

        // Validate brand exists
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand not found with id: " + brandId);
        }

        List<Dealer> dealers = dealerRepository.findByBrandId(brandId);

        return dealers.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DealerDetailResponse getDealerById(Long dealerId) {
        log.debug("Getting dealer by id: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + dealerId));

        return convertToDetailResponse(dealer);
    }

    @Override
    @Transactional
    public DealerDetailResponse createDealer(DealerRequest request) {
        log.info("Creating dealer: {}", request.getDealerName());

        // Validate brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()));

        // Validate email uniqueness (if provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (dealerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException(
                        "Email already exists: " + request.getEmail());
            }
        }

        // Create dealer
        Dealer dealer = Dealer.builder()
                .dealerName(request.getDealerName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .dealerLevel(request.getDealerLevel())
                .brand(brand)
                .build();

        Dealer savedDealer = dealerRepository.save(dealer);

        log.info("Dealer created successfully with id: {}", savedDealer.getId());

        return convertToDetailResponse(savedDealer);
    }

    @Override
    @Transactional
    public DealerDetailResponse updateDealer(Long dealerId, DealerRequest request) {
        log.info("Updating dealer id: {}", dealerId);

        // Find existing dealer
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + dealerId));

        // Validate brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()));

        // Validate email uniqueness (if changed)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (dealerRepository.existsByEmailAndIdNot(request.getEmail(), dealerId)) {
                throw new IllegalArgumentException(
                        "Email already exists: " + request.getEmail());
            }
        }

        // Update fields
        dealer.setDealerName(request.getDealerName());
        dealer.setAddress(request.getAddress());
        dealer.setPhoneNumber(request.getPhoneNumber());
        dealer.setEmail(request.getEmail());
        dealer.setDealerLevel(request.getDealerLevel());
        dealer.setBrand(brand);

        Dealer updatedDealer = dealerRepository.save(dealer);

        log.info("Dealer updated successfully: {}", dealerId);

        return convertToDetailResponse(updatedDealer);
    }

    @Override
    @Transactional
    public void deleteDealer(Long dealerId) {
        log.info("Deleting dealer id: {}", dealerId);

        // Check if dealer exists
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + dealerId));

        // Check if dealer has associated data
        Long usersCount = dealerRepository.countUsersByDealerId(dealerId);
        Long vehiclesCount = dealerRepository.countVehiclesByDealerId(dealerId);
        Long appointmentsCount = dealerRepository.countAppointmentsByDealerId(dealerId);

        if (usersCount > 0 || vehiclesCount > 0 || appointmentsCount > 0) {
            throw new IllegalStateException(
                    String.format("Cannot delete dealer. It has %d users, %d vehicles, and %d appointments",
                            usersCount, vehiclesCount, appointmentsCount));
        }

        // Delete dealer
        dealerRepository.deleteById(dealerId);

        log.info("Dealer deleted successfully: {}", dealerId);
    }

    /**
     * Convert Dealer entity to DealerListResponse
     */
    private DealerListResponse convertToListResponse(Dealer dealer) {
        // Get statistics
        Long totalUsers = dealerRepository.countUsersByDealerId(dealer.getId());
        Long totalVehicles = dealerRepository.countVehiclesByDealerId(dealer.getId());
        Long totalAppointments = dealerRepository.countAppointmentsByDealerId(dealer.getId());

        return DealerListResponse.builder()
                .id(dealer.getId())
                .dealerName(dealer.getDealerName())
                .address(dealer.getAddress())
                .phoneNumber(dealer.getPhoneNumber())
                .email(dealer.getEmail())
                .dealerLevel(dealer.getDealerLevel())
                .brandId(dealer.getBrand() != null ? dealer.getBrand().getId() : null)
                .brandName(dealer.getBrand() != null ? dealer.getBrand().getBrandName() : null)
                .totalUsers(totalUsers)
                .totalVehicles(totalVehicles)
                .totalAppointments(totalAppointments)
                .build();
    }

    /**
     * Convert Dealer entity to DealerDetailResponse
     */
    private DealerDetailResponse convertToDetailResponse(Dealer dealer) {
        // Get statistics
        Long totalUsers = dealerRepository.countUsersByDealerId(dealer.getId());
        Long totalVehicles = dealerRepository.countVehiclesByDealerId(dealer.getId());
        Long totalAppointments = dealerRepository.countAppointmentsByDealerId(dealer.getId());
        Long totalSellInRequests = dealerRepository.countSellInRequestsByDealerId(dealer.getId());
        Long totalContracts = dealerRepository.countContractsByDealerId(dealer.getId());

        return DealerDetailResponse.builder()
                .id(dealer.getId())
                .dealerName(dealer.getDealerName())
                .address(dealer.getAddress())
                .phoneNumber(dealer.getPhoneNumber())
                .email(dealer.getEmail())
                .dealerLevel(dealer.getDealerLevel())
                .brandId(dealer.getBrand() != null ? dealer.getBrand().getId() : null)
                .brandName(dealer.getBrand() != null ? dealer.getBrand().getBrandName() : null)
                .totalUsers(totalUsers)
                .totalVehicles(totalVehicles)
                .totalAppointments(totalAppointments)
                .totalSellInRequests(totalSellInRequests)
                .totalContracts(totalContracts)
                .build();
    }

    /**
     * Build Pageable with sorting
     */
    private Pageable buildPageable(DealerFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;

        // Validate page size (max 100)
        if (size > 100) {
            size = 100;
        }

        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null) {
            switch (filterRequest.getSortBy()) {
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "dealerName");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "dealerName");
                    break;
                case "level_asc":
                    sort = Sort.by(Sort.Direction.ASC, "dealerLevel");
                    break;
                case "level_desc":
                    sort = Sort.by(Sort.Direction.DESC, "dealerLevel");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.ASC, "id");
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
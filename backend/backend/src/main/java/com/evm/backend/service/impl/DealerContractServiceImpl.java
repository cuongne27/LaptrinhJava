package com.evm.backend.service.impl;

import com.evm.backend.dto.request.DealerContractFilterRequest;
import com.evm.backend.dto.request.DealerContractRequest;
import com.evm.backend.dto.response.DealerContractDetailResponse;
import com.evm.backend.dto.response.DealerContractListResponse;
import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.DealerContract;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.BrandRepository;
import com.evm.backend.repository.DealerContractRepository;
import com.evm.backend.repository.DealerRepository;
import com.evm.backend.service.DealerContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of DealerContractService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DealerContractServiceImpl implements DealerContractService {

    private final DealerContractRepository contractRepository;
    private final BrandRepository brandRepository;
    private final DealerRepository dealerRepository;

    @Override
    public Page<DealerContractListResponse> getAllContracts(DealerContractFilterRequest filterRequest) {
        log.debug("Getting all contracts with filters: {}", filterRequest);

        Pageable pageable = buildPageable(filterRequest);

        Page<DealerContract> contractsPage;

        // Filter by status if provided
        if (filterRequest.getStatus() != null) {
            LocalDate currentDate = LocalDate.now();
            switch (filterRequest.getStatus().toUpperCase()) {
                case "ACTIVE":
                    contractsPage = contractRepository.findContractsWithFilters(
                            filterRequest.getBrandId(),
                            filterRequest.getDealerId(),
                            null,
                            currentDate,
                            pageable
                    );
                    // Further filter for active (tricky with pagination, better to add to query)
                    break;
                case "EXPIRED":
                    contractsPage = contractRepository.findExpiredContracts(currentDate, pageable);
                    break;
                case "UPCOMING":
                    contractsPage = contractRepository.findUpcomingContracts(currentDate, pageable);
                    break;
                default:
                    contractsPage = contractRepository.findContractsWithFilters(
                            filterRequest.getBrandId(),
                            filterRequest.getDealerId(),
                            filterRequest.getStartDate(),
                            filterRequest.getEndDate(),
                            pageable
                    );
            }
        } else {
            contractsPage = contractRepository.findContractsWithFilters(
                    filterRequest.getBrandId(),
                    filterRequest.getDealerId(),
                    filterRequest.getStartDate(),
                    filterRequest.getEndDate(),
                    pageable
            );
        }

        return contractsPage.map(this::convertToListResponse);
    }

    @Override
    public List<DealerContractListResponse> getContractsByDealer(Long dealerId) {
        log.debug("Getting contracts by dealerId: {}", dealerId);

        // Validate dealer exists
        if (!dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException("Dealer not found with id: " + dealerId);
        }

        List<DealerContract> contracts = contractRepository.findByDealerId(dealerId);

        return contracts.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DealerContractListResponse> getContractsByBrand(Integer brandId) {
        log.debug("Getting contracts by brandId: {}", brandId);

        // Validate brand exists
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand not found with id: " + brandId);
        }

        List<DealerContract> contracts = contractRepository.findByBrandId(brandId);

        return contracts.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DealerContractDetailResponse getContractById(Long contractId) {
        log.debug("Getting contract by id: {}", contractId);

        DealerContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found with id: " + contractId));

        return convertToDetailResponse(contract);
    }

    @Override
    @Transactional
    public DealerContractDetailResponse createContract(DealerContractRequest request) {
        log.info("Creating contract for dealer: {}", request.getDealerId());

        // Validate brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()));

        // Validate dealer exists
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + request.getDealerId()));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check for overlapping contracts
        if (contractRepository.hasOverlappingContractForNew(
                request.getDealerId(),
                request.getStartDate(),
                request.getEndDate())) {
            throw new IllegalArgumentException(
                    "Contract dates overlap with an existing contract for this dealer");
        }

        // Create contract
        DealerContract contract = DealerContract.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .contractTerms(request.getContractTerms())
                .commissionRate(request.getCommissionRate())
                .salesTarget(request.getSalesTarget())
                .brand(brand)
                .dealer(dealer)
                .build();

        DealerContract savedContract = contractRepository.save(contract);

        log.info("Contract created successfully with id: {}", savedContract.getId());

        return convertToDetailResponse(savedContract);
    }

    @Override
    @Transactional
    public DealerContractDetailResponse updateContract(Long contractId, DealerContractRequest request) {
        log.info("Updating contract id: {}", contractId);

        // Find existing contract
        DealerContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found with id: " + contractId));

        // Validate brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()));

        // Validate dealer exists
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer not found with id: " + request.getDealerId()));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check for overlapping contracts (exclude current contract)
        if (contractRepository.hasOverlappingContract(
                request.getDealerId(),
                contractId,
                request.getStartDate(),
                request.getEndDate())) {
            throw new IllegalArgumentException(
                    "Contract dates overlap with an existing contract for this dealer");
        }

        // Update fields
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setContractTerms(request.getContractTerms());
        contract.setCommissionRate(request.getCommissionRate());
        contract.setSalesTarget(request.getSalesTarget());
        contract.setBrand(brand);
        contract.setDealer(dealer);

        DealerContract updatedContract = contractRepository.save(contract);

        log.info("Contract updated successfully: {}", contractId);

        return convertToDetailResponse(updatedContract);
    }

    @Override
    @Transactional
    public void deleteContract(Long contractId) {
        log.info("Deleting contract id: {}", contractId);

        // Check if contract exists
        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contract not found with id: " + contractId);
        }

        // Delete contract
        contractRepository.deleteById(contractId);

        log.info("Contract deleted successfully: {}", contractId);
    }

    /**
     * Convert DealerContract entity to DealerContractListResponse
     */
    private DealerContractListResponse convertToListResponse(DealerContract contract) {
        String status = calculateStatus(contract.getStartDate(), contract.getEndDate());
        Long daysRemaining = calculateDaysRemaining(contract.getEndDate());
        Long totalDays = ChronoUnit.DAYS.between(contract.getStartDate(), contract.getEndDate());

        return DealerContractListResponse.builder()
                .id(contract.getId())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .commissionRate(contract.getCommissionRate())
                .salesTarget(contract.getSalesTarget())
                .status(status)
                .brandId(contract.getBrand() != null ? contract.getBrand().getId() : null)
                .brandName(contract.getBrand() != null ? contract.getBrand().getBrandName() : null)
                .dealerId(contract.getDealer() != null ? contract.getDealer().getId() : null)
                .dealerName(contract.getDealer() != null ? contract.getDealer().getDealerName() : null)
                .daysRemaining(daysRemaining)
                .totalDays(totalDays)
                .build();
    }

    /**
     * Convert DealerContract entity to DealerContractDetailResponse
     */
    private DealerContractDetailResponse convertToDetailResponse(DealerContract contract) {
        String status = calculateStatus(contract.getStartDate(), contract.getEndDate());
        Long daysRemaining = calculateDaysRemaining(contract.getEndDate());
        Long totalDays = ChronoUnit.DAYS.between(contract.getStartDate(), contract.getEndDate());
        Integer progressPercentage = calculateProgressPercentage(
                contract.getStartDate(), contract.getEndDate());

        return DealerContractDetailResponse.builder()
                .id(contract.getId())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractTerms(contract.getContractTerms())
                .commissionRate(contract.getCommissionRate())
                .salesTarget(contract.getSalesTarget())
                .status(status)
                .brandId(contract.getBrand() != null ? contract.getBrand().getId() : null)
                .brandName(contract.getBrand() != null ? contract.getBrand().getBrandName() : null)
                .dealerId(contract.getDealer() != null ? contract.getDealer().getId() : null)
                .dealerName(contract.getDealer() != null ? contract.getDealer().getDealerName() : null)
                .dealerAddress(contract.getDealer() != null ? contract.getDealer().getAddress() : null)
                .dealerLevel(contract.getDealer() != null ? contract.getDealer().getDealerLevel() : null)
                .daysRemaining(daysRemaining)
                .totalDays(totalDays)
                .progressPercentage(progressPercentage)
                .build();
    }

    /**
     * Calculate contract status
     */
    private String calculateStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();

        if (currentDate.isBefore(startDate)) {
            return "UPCOMING";
        } else if (currentDate.isAfter(endDate)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }

    /**
     * Calculate days remaining (only for active contracts)
     */
    private Long calculateDaysRemaining(LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();

        if (currentDate.isAfter(endDate)) {
            return 0L;
        }

        return ChronoUnit.DAYS.between(currentDate, endDate);
    }

    /**
     * Calculate progress percentage
     */
    private Integer calculateProgressPercentage(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();

        if (currentDate.isBefore(startDate)) {
            return 0;
        } else if (currentDate.isAfter(endDate)) {
            return 100;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, currentDate);

        return (int) ((elapsedDays * 100) / totalDays);
    }

    /**
     * Build Pageable with sorting
     */
    private Pageable buildPageable(DealerContractFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;

        if (size > 100) {
            size = 100;
        }

        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null) {
            switch (filterRequest.getSortBy()) {
                case "start_date_asc":
                    sort = Sort.by(Sort.Direction.ASC, "startDate");
                    break;
                case "start_date_desc":
                    sort = Sort.by(Sort.Direction.DESC, "startDate");
                    break;
                case "end_date_asc":
                    sort = Sort.by(Sort.Direction.ASC, "endDate");
                    break;
                case "end_date_desc":
                    sort = Sort.by(Sort.Direction.DESC, "endDate");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "startDate");
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
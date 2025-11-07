package com.evm.backend.service.impl;

import com.evm.backend.dto.request.BrandRequest;
import com.evm.backend.dto.response.BrandDetailResponse;
import com.evm.backend.dto.response.BrandListResponse;
import com.evm.backend.entity.Brand;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.BrandRepository;
import com.evm.backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BrandService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public Page<BrandListResponse> getAllBrands(Pageable pageable) {
        log.debug("Getting all brands with pagination: {}", pageable);

        Page<Brand> brandsPage = brandRepository.findAll(pageable);

        return brandsPage.map(this::convertToListResponse);
    }

    @Override
    public List<BrandListResponse> getAllBrands() {
        log.debug("Getting all brands without pagination");

        List<Brand> brands = brandRepository.findAll();

        return brands.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDetailResponse getBrandById(Integer brandId) {
        log.debug("Getting brand by id: {}", brandId);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + brandId));

        return convertToDetailResponse(brand);
    }

    @Override
    @Transactional
    public BrandDetailResponse createBrand(BrandRequest request) {
        log.info("Creating brand: {}", request.getBrandName());

        // Validate brand name uniqueness
        if (brandRepository.existsByBrandName(request.getBrandName())) {
            throw new IllegalArgumentException(
                    "Brand name already exists: " + request.getBrandName());
        }

        // Validate tax code uniqueness (if provided)
        if (request.getTaxCode() != null && !request.getTaxCode().isEmpty()) {
            if (brandRepository.existsByTaxCode(request.getTaxCode())) {
                throw new IllegalArgumentException(
                        "Tax code already exists: " + request.getTaxCode());
            }
        }

        // Create brand
        Brand brand = Brand.builder()
                .brandName(request.getBrandName())
                .headquartersAddress(request.getHeadquartersAddress())
                .taxCode(request.getTaxCode())
                .contactInfo(request.getContactInfo())
                .build();

        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully with id: {}", savedBrand.getId());

        return convertToDetailResponse(savedBrand);
    }

    @Override
    @Transactional
    public BrandDetailResponse updateBrand(Integer brandId, BrandRequest request) {
        log.info("Updating brand id: {}", brandId);

        // Find existing brand
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + brandId));

        // Validate brand name uniqueness (exclude current brand)
        if (brandRepository.existsByBrandNameAndIdNot(request.getBrandName(), brandId)) {
            throw new IllegalArgumentException(
                    "Brand name already exists: " + request.getBrandName());
        }

        // Update fields
        brand.setBrandName(request.getBrandName());
        brand.setHeadquartersAddress(request.getHeadquartersAddress());
        brand.setTaxCode(request.getTaxCode());
        brand.setContactInfo(request.getContactInfo());

        Brand updatedBrand = brandRepository.save(brand);

        log.info("Brand updated successfully: {}", brandId);

        return convertToDetailResponse(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(Integer brandId) {
        log.info("Deleting brand id: {}", brandId);

        // Check if brand exists
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + brandId));

        // Check if brand has associated data
        Long dealersCount = brandRepository.countDealersByBrandId(brandId);
        Long productsCount = brandRepository.countProductsByBrandId(brandId);
        Long usersCount = brandRepository.countUsersByBrandId(brandId);

        if (dealersCount > 0 || productsCount > 0 || usersCount > 0) {
            throw new IllegalStateException(
                    String.format("Cannot delete brand. It has %d dealers, %d products, and %d users",
                            dealersCount, productsCount, usersCount));
        }

        // Delete brand
        brandRepository.deleteById(brandId);

        log.info("Brand deleted successfully: {}", brandId);
    }

    /**
     * Convert Brand entity to BrandListResponse
     */
    private BrandListResponse convertToListResponse(Brand brand) {
        // Get statistics
        Long totalDealers = brandRepository.countDealersByBrandId(brand.getId());
        Long totalProducts = brandRepository.countProductsByBrandId(brand.getId());
        Long totalUsers = brandRepository.countUsersByBrandId(brand.getId());

        return BrandListResponse.builder()
                .id(brand.getId())
                .brandName(brand.getBrandName())
                .headquartersAddress(brand.getHeadquartersAddress())
                .taxCode(brand.getTaxCode())
                .contactInfo(brand.getContactInfo())
                .totalDealers(totalDealers)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .build();
    }

    /**
     * Convert Brand entity to BrandDetailResponse
     */
    private BrandDetailResponse convertToDetailResponse(Brand brand) {
        // Get statistics
        Long totalDealers = brandRepository.countDealersByBrandId(brand.getId());
        Long totalProducts = brandRepository.countProductsByBrandId(brand.getId());
        Long totalUsers = brandRepository.countUsersByBrandId(brand.getId());
        Long totalContracts = brandRepository.countContractsByBrandId(brand.getId());

        return BrandDetailResponse.builder()
                .id(brand.getId())
                .brandName(brand.getBrandName())
                .headquartersAddress(brand.getHeadquartersAddress())
                .taxCode(brand.getTaxCode())
                .contactInfo(brand.getContactInfo())
                .totalDealers(totalDealers)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .totalContracts(totalContracts)
                .build();
    }
}
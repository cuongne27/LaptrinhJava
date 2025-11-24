package com.evm.backend.service.impl;

import com.evm.backend.dto.request.ProductFilterRequest;
import com.evm.backend.dto.request.ProductRequest;
import com.evm.backend.dto.response.*;
import com.evm.backend.entity.*;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.BrandRepository;
import com.evm.backend.repository.ProductRepository;
import com.evm.backend.repository.UserRepository;
import com.evm.backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ProductService
 * UC-DL-01: Xem danh mục và thông tin chi tiết xe
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BrandRepository brandRepository;

    /**
     * Get all products with pagination (for CRUD management)
     */
    @Override
    public Page<ProductListResponse> getAllProducts(Pageable pageable) {
        log.debug("Getting all products with pagination: {}", pageable);

        Page<Product> productsPage = productRepository.findAll(pageable);

        // Convert to ProductListResponse without dealer context
        return productsPage.map(product -> convertToListResponseWithoutDealer(product));
    }

    /**
     * Get all products with pagination and search (for CRUD management)
     */
    @Override
    public Page<ProductListResponse> getAllProducts(String searchKeyword, Pageable pageable) {
        log.debug("Getting all products with pagination and search: {}", pageable);

        Page<Product> productsPage;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // Search by product name with Brand fetched
            productsPage = productRepository.findByProductNameContainingIgnoreCaseWithBrand(
                    searchKeyword.trim(), pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        // Convert to ProductListResponse without dealer context
        return productsPage.map(product -> convertToListResponseWithoutDealer(product));
    }

    @Override
    public Page<ProductListResponse> getProductCatalog(
            String username,
            ProductFilterRequest filterRequest) {

        // Lấy thông tin dealer từ user
        User user = getUserWithDealer(username);
        Long dealerId = user.getDealer().getId();

        // Xây dựng Pageable với sort
        Pageable pageable = buildPageable(filterRequest);

        // Tìm kiếm với filter
        Page<Product> productsPage = productRepository.findProductsWithFilters(
                filterRequest.getBrandId(),
                filterRequest.getSearchKeyword(),
                filterRequest.getMinPrice(),
                filterRequest.getMaxPrice(),
                pageable
        );

        log.debug("Found {} products", productsPage.getTotalElements());

        // Convert sang Response DTO
        return productsPage.map(product -> convertToListResponse(product, dealerId));
    }

    //GET CHI TIẾT PRODUCT
    @Override
    public ProductDetailResponse getProductDetail(String username, Long productId) {

        log.debug("Getting product detail for user: {}, productId: {}", username, productId);

        // Lấy thông tin dealer từ user
        User user = getUserWithDealer(username);
//        Long dealerId = user.getDealer().getId();

        // Tìm product với details
        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        log.debug("Product found: {}", product.getProductName());

        return convertToDetailResponse(product);
    }

    /**
     * Get product detail by ID (for CRUD management, no dealer context)
     */
    @Override
    public ProductDetailResponse getProductById(Long productId) {
        log.debug("Getting product by id: {}", productId);

        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        log.debug("Product found: {}", product.getProductName());

        return convertToDetailResponse(product);
    }

    //TẠO PRODUCT
    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductRequest productRequest) {
        log.debug("Creating product: {}", productRequest.getProductName());

        // Validate brand existence
        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + productRequest.getBrandId()));

        // Tạo TechnicalSpecs (Embeddable nên không cần set product)
        // Nếu technicalSpecs null, tạo TechnicalSpecs rỗng
        TechnicalSpecs technicalSpecs = null;
        if (productRequest.getTechnicalSpecs() != null) {
            technicalSpecs = TechnicalSpecs.builder()
                    .batteryCapacity(productRequest.getTechnicalSpecs().getBatteryCapacity())
                    .productRange(productRequest.getTechnicalSpecs().getProductRange())
                    .power(productRequest.getTechnicalSpecs().getPower())
                    .maxSpeed(productRequest.getTechnicalSpecs().getMaxSpeed())
                    .chargingTime(productRequest.getTechnicalSpecs().getChargingTime())
                    .dimensions(productRequest.getTechnicalSpecs().getDimensions())
                    .weight(productRequest.getTechnicalSpecs().getWeight())
                    .seatingCapacity(productRequest.getTechnicalSpecs().getSeatingCapacity())
                    .build();
        } else {
            // Tạo TechnicalSpecs rỗng nếu không có
            technicalSpecs = TechnicalSpecs.builder()
                    .batteryCapacity(null)
                    .productRange(null)
                    .power(null)
                    .maxSpeed(null)
                    .chargingTime(null)
                    .dimensions(null)
                    .weight(null)
                    .seatingCapacity(null)
                    .build();
        }

        // Tạo Product với TechnicalSpecs nhúng
        Product product = Product.builder()
                .productName(productRequest.getProductName())
                .version(productRequest.getVersion())
                .msrp(productRequest.getMsrp())
                .description(productRequest.getDescription())
                .imageUrl(productRequest.getImageUrl())
                .videoUrl(productRequest.getVideoUrl())
                .brand(brand)
                .isActive(productRequest.getIsActive() != null ? productRequest.getIsActive() : true)
                .technicalSpecs(technicalSpecs)
                .features(new ArrayList<>())
                .variants(new ArrayList<>())
                .build();

        // Tạo và set Features (ManyToOne nên cần set product)
        if (productRequest.getFeatures() != null && !productRequest.getFeatures().isEmpty()) {
            List<ProductFeature> features = productRequest.getFeatures().stream()
                    .map(featureRequest -> ProductFeature.builder()
                            .featureName(featureRequest.getFeatureName())
                            .description(featureRequest.getDescription())
                            .iconUrl(featureRequest.getIconUrl())
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.setFeatures(features);
        }

        // Tạo và set Variants (ManyToOne nên cần set product)
        if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
            List<ProductVariant> variants = productRequest.getVariants().stream()
                    .map(variantRequest -> ProductVariant.builder()
                            .color(variantRequest.getColor())
                            .colorCode(variantRequest.getColorCode())
                            .availableQuantity(variantRequest.getAvailableQuantity())
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.setVariants(variants);
        }

        // Lưu product (cascade sẽ lưu cả Features và Variants)
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());

        // Convert và return response
        return convertToDetailResponse(savedProduct);
    }

    /**
     * UPDATE - Cập nhật sản phẩm
     */
    @Transactional
    @Override
    public ProductDetailResponse updateProduct(Long productId, ProductRequest productRequest) {
        log.info("Updating product id: {}, name: {}", productId, productRequest.getProductName());

        // Tìm product hiện tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        // Validate brand existence
        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + productRequest.getBrandId()));

        // Update basic fields
        product.setProductName(productRequest.getProductName());
        product.setVersion(productRequest.getVersion());
        product.setMsrp(productRequest.getMsrp());
        product.setDescription(productRequest.getDescription());
        product.setImageUrl(productRequest.getImageUrl());
        product.setVideoUrl(productRequest.getVideoUrl());
        product.setBrand(brand);
        product.setIsActive(productRequest.getIsActive() != null ? productRequest.getIsActive() : true);

        // Update TechnicalSpecs (Embeddable)
        if (productRequest.getTechnicalSpecs() != null) {
            TechnicalSpecs technicalSpecs = TechnicalSpecs.builder()
                    .batteryCapacity(productRequest.getTechnicalSpecs().getBatteryCapacity())
                    .productRange(productRequest.getTechnicalSpecs().getProductRange())
                    .power(productRequest.getTechnicalSpecs().getPower())
                    .maxSpeed(productRequest.getTechnicalSpecs().getMaxSpeed())
                    .chargingTime(productRequest.getTechnicalSpecs().getChargingTime())
                    .dimensions(productRequest.getTechnicalSpecs().getDimensions())
                    .weight(productRequest.getTechnicalSpecs().getWeight())
                    .seatingCapacity(productRequest.getTechnicalSpecs().getSeatingCapacity())
                    .build();
            product.setTechnicalSpecs(technicalSpecs);
        } else {
            // Nếu technicalSpecs null, tạo TechnicalSpecs rỗng
            TechnicalSpecs emptySpecs = TechnicalSpecs.builder()
                    .batteryCapacity(null)
                    .productRange(null)
                    .power(null)
                    .maxSpeed(null)
                    .chargingTime(null)
                    .dimensions(null)
                    .weight(null)
                    .seatingCapacity(null)
                    .build();
            product.setTechnicalSpecs(emptySpecs);
        }

        // Update Features: Clear và rebuild
        product.getFeatures().clear();
        if (productRequest.getFeatures() != null && !productRequest.getFeatures().isEmpty()) {
            List<ProductFeature> newFeatures = productRequest.getFeatures().stream()
                    .map(featureRequest -> ProductFeature.builder()
                            .featureName(featureRequest.getFeatureName())
                            .description(featureRequest.getDescription())
                            .iconUrl(featureRequest.getIconUrl())
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.getFeatures().addAll(newFeatures);
        }

        // Update Variants: Clear và rebuild
        product.getVariants().clear();
        if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
            List<ProductVariant> newVariants = productRequest.getVariants().stream()
                    .map(variantRequest -> ProductVariant.builder()
                            .color(variantRequest.getColor())
                            .colorCode(variantRequest.getColorCode())
                            .availableQuantity(variantRequest.getAvailableQuantity() != null
                                    ? variantRequest.getAvailableQuantity() : 0L)
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.getVariants().addAll(newVariants);
        }

        // Save product (cascade sẽ update features và variants)
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with id: {}", updatedProduct.getId());

        // Convert và return response
        return convertToDetailResponse(updatedProduct);
    }

    /**
     * SOFT DELETE - Đặt isActive = false
     */
    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        log.info("Soft deleting product id: {}", productId);

        // Tìm product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        // Set isActive = false
        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product soft deleted successfully: {}", productId);
    }

    /**
     * HARD DELETE - Xóa vĩnh viễn khỏi database
     */
    @Transactional
    @Override
    public void hardDeleteProduct(Long productId) {
        log.info("Hard deleting product id: {}", productId);

        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Delete product (cascade sẽ xóa features và variants)
        productRepository.deleteById(productId);

        log.info("Product hard deleted successfully: {}", productId);
    }

    /**
     * Get user with dealer validation
     */
    private User getUserWithDealer(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

//        if (user.getDealer() == null) {
//            throw new IllegalStateException("User is not associated with any dealer");
//        }

        return user;
    }

    /**
     * Convert Product entity to ProductListResponse (with dealer context)
     */
    private ProductListResponse convertToListResponse(Product product, Long dealerId) {
        // Lấy màu sắc có sẵn tại dealer
        List<String> availableColors = productRepository
                .findAvailableColorsByProductAndDealer(product.getId(), dealerId);

        // Đếm số lượng xe có sẵn
        Long availableQuantity = productRepository
                .countAvailableVehiclesByProductAndDealer(product.getId(), dealerId);

        return ProductListResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .version(product.getVersion())
                .msrp(product.getMsrp())
                .imageUrl(product.getImageUrl())
                .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                .brandId(product.getBrand() != null ? Long.valueOf(product.getBrand().getId()) : null)
                .availableColors(availableColors)
                .availableQuantity(availableQuantity)
                .isActive(product.getIsActive())
                .build();
    }

    /**
     * Convert Product entity to ProductListResponse (without dealer context)
     */
    private ProductListResponse convertToListResponseWithoutDealer(Product product) {
        // Lấy màu sắc từ variants
        List<String> availableColors = new ArrayList<>();
        if (product.getVariants() != null) {
            availableColors = product.getVariants().stream()
                    .map(variant -> variant.getColor())
                    .filter(color -> color != null)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // Tính tổng số lượng từ variants
        Long availableQuantity = 0L;
        if (product.getVariants() != null) {
            availableQuantity = product.getVariants().stream()
                    .mapToLong(variant -> variant.getAvailableQuantity() != null ? variant.getAvailableQuantity() : 0L)
                    .sum();
        }

        return ProductListResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .version(product.getVersion())
                .msrp(product.getMsrp())
                .imageUrl(product.getImageUrl())
                .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                .brandId(product.getBrand() != null ? Long.valueOf(product.getBrand().getId()) : null)
                .availableColors(availableColors)
                .availableQuantity(availableQuantity)
                .isActive(product.getIsActive())
                .build();
    }

    /**
     * Convert Product entity to ProductDetailResponse
     */
    private ProductDetailResponse convertToDetailResponse(Product product) {

        // Lấy TechnicalSpecs từ embedded object
        TechnicalSpecsResponse technicalSpecsResponse = null;
        if (product.getTechnicalSpecs() != null) {
            TechnicalSpecs specs = product.getTechnicalSpecs();
            technicalSpecsResponse = TechnicalSpecsResponse.builder()
                    .batteryCapacity(specs.getBatteryCapacity())
                    .productRange(specs.getProductRange())
                    .power(specs.getPower())
                    .maxSpeed(specs.getMaxSpeed())
                    .chargingTime(specs.getChargingTime())
                    .dimensions(specs.getDimensions())
                    .weight(specs.getWeight())
                    .seatingCapacity(specs.getSeatingCapacity())
                    .build();
        }

        // Lấy variants
        List<ProductVariantResponse> variantResponses = new ArrayList<>();
//        if (dealerId != null) {
//            // Nếu có dealerId, lấy từ repository với số lượng tại dealer
//            variantResponses = getProductVariants(product.getId(), dealerId);
//        } else {
            // Nếu không có dealerId (khi tạo mới), lấy từ product entity
            if (product.getVariants() != null) {
                variantResponses = product.getVariants().stream()
                        .map(variant -> ProductVariantResponse.builder()
                                .color(variant.getColor())
                                .colorCode(variant.getColorCode())
                                .availableQuantity(variant.getAvailableQuantity())
                                .build())
                        .collect(Collectors.toList());
            }
//        }

        // Lấy features từ product entity
        List<ProductFeatureResponse> featureResponses = new ArrayList<>();
        if (product.getFeatures() != null) {
            featureResponses = product.getFeatures().stream()
                    .map(feature -> ProductFeatureResponse.builder()
                            .featureName(feature.getFeatureName())
                            .description(feature.getDescription())
                            .iconUrl(feature.getIconUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductDetailResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .version(product.getVersion())
                .msrp(product.getMsrp())
                .specifications(product.getSpecifications())
                .description(product.getDescription())
                .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                .brandId(product.getBrand() != null ? Long.valueOf(product.getBrand().getId()) : null)
                .imageUrl(product.getImageUrl())
                .videoUrl(product.getVideoUrl())
                .variants(variantResponses)
                .features(featureResponses)
                .technicalSpecs(technicalSpecsResponse)
                .isActive(product.getIsActive())
                .build();
    }

    /**
     * Get product variants (colors with quantity)
     */
    private List<ProductVariantResponse> getProductVariants(Long productId, Long dealerId) {
        List<Object[]> results = productRepository
                .countAvailableVehiclesByColorAndDealer(productId, dealerId);

        return results.stream()
                .map(result -> ProductVariantResponse.builder()
                        .color((String) result[0])
                        .availableQuantity((Long) result[1])
                        .colorCode(getColorCode((String) result[0]))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Parse specifications JSON to TechnicalSpecsResponse
     * (Deprecated - sử dụng TechnicalSpecs embedded thay thế)
     */
    private TechnicalSpecsResponse parseSpecifications(String specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return new TechnicalSpecsResponse();
        }

        try {
            Map<String, String> specsMap = objectMapper.readValue(
                    specifications,
                    Map.class
            );

            return TechnicalSpecsResponse.builder()
                    .batteryCapacity(specsMap.get("battery_capacity"))
                    .productRange(specsMap.get("range"))
                    .power(specsMap.get("power"))
                    .maxSpeed(specsMap.get("max_speed"))
                    .chargingTime(specsMap.get("charging_time"))
                    .dimensions(specsMap.get("dimensions"))
                    .weight(specsMap.get("weight"))
                    .seatingCapacity(specsMap.get("seating_capacity"))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse specifications JSON: {}", e.getMessage());
            return new TechnicalSpecsResponse();
        }
    }

    /**
     * Parse features from product
     * (Deprecated - sử dụng ProductFeature entity thay thế)
     */
    private List<ProductFeatureResponse> parseFeatures(Product product) {
        List<ProductFeatureResponse> features = new ArrayList<>();

        if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
            return product.getFeatures().stream()
                    .map(feature -> ProductFeatureResponse.builder()
                            .featureName(feature.getFeatureName())
                            .description(feature.getDescription())
                            .iconUrl(feature.getIconUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        return features;
    }

    /**
     * Get product variants WITH dealer context (số lượng xe thực tế tại dealer)
     */
    private List<ProductVariantResponse> getProductVariantsWithDealer(Long productId, Long dealerId) {
        List<Object[]> results = productRepository
                .countAvailableVehiclesByColorAndDealer(productId, dealerId);

        return results.stream()
                .map(result -> ProductVariantResponse.builder()
                        .color((String) result[0])
                        .availableQuantity((Long) result[1])
                        .colorCode(getColorCode((String) result[0]))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get product variants WITHOUT dealer context (từ ProductVariant entity)
     */
    private List<ProductVariantResponse> getProductVariantsWithoutDealer(Long productId) {
        // Query variants từ database theo productId
        // Giả sử bạn có ProductVariantRepository
        // List<ProductVariant> variants = productVariantRepository.findByProductId(productId);

        // Tạm thời lấy từ product entity (nếu đã load)
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getVariants() == null) {
            return new ArrayList<>();
        }

        return product.getVariants().stream()
                .map(variant -> ProductVariantResponse.builder()
                        .color(variant.getColor())
                        .colorCode(variant.getColorCode())
                        .availableQuantity(variant.getAvailableQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map color name to hex code
     */
    private String getColorCode(String colorName) {
        if (colorName == null) {
            return "#CCCCCC";
        }

        Map<String, String> colorMap = Map.of(
                "White", "#FFFFFF",
                "Black", "#000000",
                "Red", "#FF0000",
                "Blue", "#0000FF",
                "Silver", "#C0C0C0",
                "Gray", "#808080",
                "Green", "#00FF00",
                "Yellow", "#FFFF00",
                "Orange", "#FFA500",
                "Brown", "#8B4513"
        );

        return colorMap.getOrDefault(colorName, "#CCCCCC");
    }

    /**
     * Build Pageable with sorting
     */
    private Pageable buildPageable(ProductFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;

        // Validate page size (max 100)
        if (size > 100) {
            size = 100;
        }

        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null) {
            switch (filterRequest.getSortBy()) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "msrp");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "msrp");
                    break;
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "productName");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "productName");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.ASC, "id");
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
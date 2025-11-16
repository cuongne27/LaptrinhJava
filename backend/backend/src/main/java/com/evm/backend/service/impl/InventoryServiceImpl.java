package com.evm.backend.service.impl;

import com.evm.backend.dto.request.InventoryFilterRequest;
import com.evm.backend.dto.request.InventoryRequest;
import com.evm.backend.dto.response.InventoryDetailResponse;
import com.evm.backend.dto.response.InventoryListResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.InventoryService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final DealerRepository dealerRepository;

    private static final Integer LOW_STOCK_THRESHOLD = 5;

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryListResponse> getAllInventory(InventoryFilterRequest filterRequest) {
        log.info("Getting all inventory with filter: {}", filterRequest);

        Specification<Inventory> spec = buildSpecification(filterRequest);
        Pageable pageable = buildPageable(filterRequest);

        Page<Inventory> inventories = inventoryRepository.findAll(spec, pageable);

        return inventories.map(this::convertToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryListResponse> getInventoryByProduct(Long productId) {
        log.info("Getting inventory for product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        List<Inventory> inventories = inventoryRepository.findByProductIdOrderByUpdatedAtDesc(productId);

        return inventories.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryListResponse> getInventoryByDealer(Long dealerId) {
        log.info("Getting inventory for dealer: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + dealerId));

        List<Inventory> inventories = inventoryRepository.findByDealerIdOrderByUpdatedAtDesc(dealerId);

        return inventories.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryListResponse> getBrandWarehouseInventory() {
        log.info("Getting brand warehouse inventory");

        List<Inventory> inventories = inventoryRepository.findByDealerIsNullOrderByUpdatedAtDesc();

        return inventories.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryListResponse> getLowStockInventory(Integer threshold) {
        log.info("Getting low stock inventory (threshold: {})", threshold);

        Integer actualThreshold = threshold != null ? threshold : LOW_STOCK_THRESHOLD;
        List<Inventory> inventories = inventoryRepository.findLowStockInventory(actualThreshold);

        return inventories.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDetailResponse getInventoryById(Long inventoryId) {
        log.info("Getting inventory by id: {}", inventoryId);

        Inventory inventory = inventoryRepository.findByIdWithDetails(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        return convertToDetailResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDetailResponse getInventoryByProductAndDealer(Long productId, Long dealerId) {
        log.info("Getting inventory for product {} at dealer {}", productId, dealerId);

        Inventory inventory = inventoryRepository.findByProductIdAndDealerId(productId, dealerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Inventory not found for product %d at dealer %d", productId, dealerId)));

        return convertToDetailResponse(inventory);
    }

    @Override
    public InventoryDetailResponse createInventory(InventoryRequest request) {
        log.info("Creating inventory for product: {} at dealer: {}", request.getProductId(), request.getDealerId());

        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        // Validate dealer (if not brand warehouse)
        Dealer dealer = null;
        if (request.getDealerId() != null) {
            dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + request.getDealerId()));
        }

        // Check if inventory already exists
        Optional<Inventory> existing = inventoryRepository.findByProductIdAndDealerId(
                request.getProductId(), request.getDealerId());
        if (existing.isPresent()) {
            throw new BadRequestException("Inventory already exists for this product and dealer");
        }

        // Validate quantities
        validateQuantities(request);

        // Create inventory
        Inventory inventory = Inventory.builder()
                .product(product)
                .dealer(dealer)
                .totalQuantity(request.getTotalQuantity())
                .reservedQuantity(request.getReservedQuantity())
                .availableQuantity(request.getAvailableQuantity())
                .inTransitQuantity(request.getInTransitQuantity())
                .location(request.getLocation())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Inventory created successfully: {}", savedInventory.getId());

        return convertToDetailResponse(savedInventory);
    }

    @Override
    public InventoryDetailResponse updateInventory(Long inventoryId, InventoryRequest request) {
        log.info("Updating inventory: {}", inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        // Validate quantities
        validateQuantities(request);

        // Update product (if changed)
        if (!inventory.getProduct().getId().equals(request.getProductId())) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
            inventory.setProduct(product);
        }

        // Update dealer (if changed)
        if (request.getDealerId() != null) {
            if (inventory.getDealer() == null || !inventory.getDealer().getId().equals(request.getDealerId())) {
                Dealer dealer = dealerRepository.findById(request.getDealerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + request.getDealerId()));
                inventory.setDealer(dealer);
            }
        } else {
            inventory.setDealer(null);
        }

        // Update quantities
        inventory.setTotalQuantity(request.getTotalQuantity());
        inventory.setReservedQuantity(request.getReservedQuantity());
        inventory.setAvailableQuantity(request.getAvailableQuantity());
        inventory.setInTransitQuantity(request.getInTransitQuantity());
        inventory.setLocation(request.getLocation());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.info("Inventory updated successfully: {}", inventoryId);

        return convertToDetailResponse(updatedInventory);
    }

    @Override
    public InventoryDetailResponse adjustQuantity(Long inventoryId, Integer quantity, String reason) {
        log.info("Adjusting inventory {} quantity by {} - Reason: {}", inventoryId, quantity, reason);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        // Adjust total and available
        int newTotal = inventory.getTotalQuantity() + quantity;
        int newAvailable = inventory.getAvailableQuantity() + quantity;

        if (newTotal < 0 || newAvailable < 0) {
            throw new BadRequestException("Cannot reduce quantity below 0");
        }

        inventory.setTotalQuantity(newTotal);
        inventory.setAvailableQuantity(newAvailable);

        Inventory adjustedInventory = inventoryRepository.save(inventory);
        log.info("Inventory quantity adjusted successfully: {}", inventoryId);

        return convertToDetailResponse(adjustedInventory);
    }

    @Override
    public InventoryDetailResponse reserveInventory(Long inventoryId, Integer quantity) {
        log.info("Reserving {} units from inventory {}", quantity, inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new BadRequestException(
                    String.format("Not enough available stock. Available: %d, Requested: %d",
                            inventory.getAvailableQuantity(), quantity));
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);

        Inventory reservedInventory = inventoryRepository.save(inventory);
        log.info("Inventory reserved successfully: {}", inventoryId);

        return convertToDetailResponse(reservedInventory);
    }

    @Override
    public InventoryDetailResponse releaseReservedInventory(Long inventoryId, Integer quantity) {
        log.info("Releasing {} reserved units from inventory {}", quantity, inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        if (inventory.getReservedQuantity() < quantity) {
            throw new BadRequestException(
                    String.format("Not enough reserved stock. Reserved: %d, Requested: %d",
                            inventory.getReservedQuantity(), quantity));
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);

        Inventory releasedInventory = inventoryRepository.save(inventory);
        log.info("Reserved inventory released successfully: {}", inventoryId);

        return convertToDetailResponse(releasedInventory);
    }

    @Override
    public InventoryDetailResponse transferInventory(Long fromInventoryId, Long toDealerId, Integer quantity) {
        log.info("Transferring {} units from inventory {} to dealer {}", quantity, fromInventoryId, toDealerId);

        // Get source inventory
        Inventory fromInventory = inventoryRepository.findById(fromInventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Source inventory not found: " + fromInventoryId));

        if (fromInventory.getAvailableQuantity() < quantity) {
            throw new BadRequestException("Not enough available stock in source inventory");
        }

        // Get destination dealer
        Dealer toDealer = dealerRepository.findById(toDealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination dealer not found: " + toDealerId));

        // Reduce from source
        fromInventory.setAvailableQuantity(fromInventory.getAvailableQuantity() - quantity);
        fromInventory.setTotalQuantity(fromInventory.getTotalQuantity() - quantity);
        fromInventory.setInTransitQuantity(fromInventory.getInTransitQuantity() + quantity);
        inventoryRepository.save(fromInventory);

        // Add to destination (or create if not exists)
        Optional<Inventory> toInventoryOpt = inventoryRepository.findByProductIdAndDealerId(
                fromInventory.getProduct().getId(), toDealerId);

        Inventory toInventory;
        if (toInventoryOpt.isPresent()) {
            toInventory = toInventoryOpt.get();
            toInventory.setInTransitQuantity(toInventory.getInTransitQuantity() + quantity);
        } else {
            toInventory = Inventory.builder()
                    .product(fromInventory.getProduct())
                    .dealer(toDealer)
                    .totalQuantity(0)
                    .reservedQuantity(0)
                    .availableQuantity(0)
                    .inTransitQuantity(quantity)
                    .build();
        }
        inventoryRepository.save(toInventory);

        log.info("Inventory transferred successfully from {} to dealer {}", fromInventoryId, toDealerId);

        return convertToDetailResponse(fromInventory);
    }

    @Override
    public void deleteInventory(Long inventoryId) {
        log.info("Deleting inventory: {}", inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        if (inventory.getTotalQuantity() > 0) {
            throw new BadRequestException("Cannot delete inventory with remaining stock");
        }

        inventoryRepository.delete(inventory);
        log.info("Inventory deleted successfully: {}", inventoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryStatistics() {
        log.info("Getting inventory statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInventoryRecords", inventoryRepository.count());
        stats.put("totalAvailableStock", inventoryRepository.getTotalAvailableQuantity());
        stats.put("totalReservedStock", inventoryRepository.getTotalReservedQuantity());
        stats.put("totalInTransitStock", inventoryRepository.getTotalInTransitQuantity());
        stats.put("lowStockCount", inventoryRepository.countLowStock(LOW_STOCK_THRESHOLD));
        stats.put("brandWarehouseCount", inventoryRepository.countByDealerIsNull());

        return stats;
    }

    // ===== HELPER METHODS =====

    private void validateQuantities(InventoryRequest request) {
        // total should >= reserved + available + inTransit
        Integer sum = request.getReservedQuantity() + request.getAvailableQuantity() + request.getInTransitQuantity();
        if (!request.getTotalQuantity().equals(sum)) {
            throw new BadRequestException(
                    String.format("Total quantity (%d) must equal reserved + available + inTransit (%d)",
                            request.getTotalQuantity(), sum));
        }
    }

    private Specification<Inventory> buildSpecification(InventoryFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search keyword
            if (filter.getSearchKeyword() != null && !filter.getSearchKeyword().isEmpty()) {
                String keyword = "%" + filter.getSearchKeyword().toLowerCase() + "%";
                Predicate productPred = cb.like(cb.lower(root.get("product").get("productName")), keyword);
                Predicate locationPred = cb.like(cb.lower(root.get("location")), keyword);
                predicates.add(cb.or(productPred, locationPred));
            }

            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getDealerId() != null) {
                predicates.add(cb.equal(root.get("dealer").get("id"), filter.getDealerId()));
            }

            if (filter.getBrandId() != null) {
                predicates.add(cb.equal(root.get("product").get("brand").get("id"), filter.getBrandId()));
            }

            if (filter.getIsBrandWarehouse() != null && filter.getIsBrandWarehouse()) {
                predicates.add(cb.isNull(root.get("dealer")));
            }

            if (filter.getMinAvailable() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("availableQuantity"), filter.getMinAvailable()));
            }

            if (filter.getMaxAvailable() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("availableQuantity"), filter.getMaxAvailable()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(InventoryFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? Math.min(filter.getSize(), 100) : 20;

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");

        if (filter.getSortBy() != null) {
            switch (filter.getSortBy()) {
                case "product_asc":
                    sort = Sort.by(Sort.Direction.ASC, "product.productName");
                    break;
                case "product_desc":
                    sort = Sort.by(Sort.Direction.DESC, "product.productName");
                    break;
                case "available_asc":
                    sort = Sort.by(Sort.Direction.ASC, "availableQuantity");
                    break;
                case "available_desc":
                    sort = Sort.by(Sort.Direction.DESC, "availableQuantity");
                    break;
                case "updated_asc":
                    sort = Sort.by(Sort.Direction.ASC, "updatedAt");
                    break;
                case "updated_desc":
                    sort = Sort.by(Sort.Direction.DESC, "updatedAt");
                    break;
                default:
                    break;
            }
        }

        return PageRequest.of(page, size, sort);
    }

    private InventoryListResponse convertToListResponse(Inventory i) {
        // Calculate stock percentage
        Double stockPercentage = i.getTotalQuantity() > 0 ?
                (i.getAvailableQuantity().doubleValue() / i.getTotalQuantity().doubleValue()) * 100 : 0.0;

        Boolean isLowStock = i.getAvailableQuantity() < LOW_STOCK_THRESHOLD;

        return InventoryListResponse.builder()
                .inventoryId(i.getId())
                // Product info
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProduct() != null ? i.getProduct().getProductName() : null)
                .productVersion(i.getProduct() != null ? i.getProduct().getVersion() : null)
                .brandName(i.getProduct() != null && i.getProduct().getBrand() != null ?
                        i.getProduct().getBrand().getBrandName() : null)
                // Dealer info
                .dealerId(i.getDealer() != null ? i.getDealer().getId() : null)
                .dealerName(i.getDealer() != null ? i.getDealer().getDealerName() : null)
                .dealerLocation(i.getDealer() != null ? i.getDealer().getAddress() : "Brand Warehouse")
                // Quantities
                .totalQuantity(i.getTotalQuantity())
                .reservedQuantity(i.getReservedQuantity())
                .availableQuantity(i.getAvailableQuantity())
                .inTransitQuantity(i.getInTransitQuantity())
                .location(i.getLocation())
                .updatedAt(i.getUpdatedAt())
                // Calculated
                .stockPercentage(stockPercentage)
                .isLowStock(isLowStock)
                .build();
    }

    private InventoryDetailResponse convertToDetailResponse(Inventory i) {
        Double stockPercentage = i.getTotalQuantity() > 0 ?
                (i.getAvailableQuantity().doubleValue() / i.getTotalQuantity().doubleValue()) * 100 : 0.0;

        Boolean isLowStock = i.getAvailableQuantity() < LOW_STOCK_THRESHOLD;

        return InventoryDetailResponse.builder()
                .inventoryId(i.getId())
                // Product info
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProduct() != null ? i.getProduct().getProductName() : null)
                .productVersion(i.getProduct() != null ? i.getProduct().getVersion() : null)
                .productDescription(i.getProduct() != null ? i.getProduct().getDescription() : null)
                .productPrice(i.getProduct() != null ? i.getProduct().getMsrp() : null)
                // Brand info
                .brandId(i.getProduct() != null && i.getProduct().getBrand() != null ?
                        i.getProduct().getBrand().getId() : null)
                .brandName(i.getProduct() != null && i.getProduct().getBrand() != null ?
                        i.getProduct().getBrand().getBrandName() : null)
                .brandContactInfo(i.getProduct() != null && i.getProduct().getBrand() != null ?
                        i.getProduct().getBrand().getContactInfo() : null)
                // Dealer info
                .dealerId(i.getDealer() != null ? i.getDealer().getId() : null)
                .dealerName(i.getDealer() != null ? i.getDealer().getDealerName() : null)
                .dealerAddress(i.getDealer() != null ? i.getDealer().getAddress() : null)
                .dealerPhone(i.getDealer() != null ? i.getDealer().getPhoneNumber() : null)
                .dealerEmail(i.getDealer() != null ? i.getDealer().getEmail() : null)
                // Quantities
                .totalQuantity(i.getTotalQuantity())
                .reservedQuantity(i.getReservedQuantity())
                .availableQuantity(i.getAvailableQuantity())
                .inTransitQuantity(i.getInTransitQuantity())
                .location(i.getLocation())
                .updatedAt(i.getUpdatedAt())
                // Statistics
                .stockPercentage(stockPercentage)
                .isLowStock(isLowStock)
                .soldQuantity(0) // TODO: Calculate from orders if needed
                .build();
    }
}
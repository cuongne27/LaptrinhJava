package com.evm.backend.controller;

import com.evm.backend.dto.request.InventoryFilterRequest;
import com.evm.backend.dto.request.InventoryRequest;
import com.evm.backend.dto.response.InventoryDetailResponse;
import com.evm.backend.dto.response.InventoryListResponse;
import com.evm.backend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CRUD operations for Inventory entity
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "APIs quản lý kho hàng (inventory)")
public class InventoryCrudController {

    private final InventoryService inventoryService;

    /**
     * GET: Lấy danh sách kho hàng với filter
     * Endpoint: GET /api/inventory
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách kho hàng", description = "Lấy danh sách kho hàng với filter và phân trang")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<InventoryListResponse>> getAllInventory(
            @Parameter(description = "Từ khóa tìm kiếm (tên sản phẩm, địa điểm)") @RequestParam(required = false) String searchKeyword,
            @Parameter(description = "Product ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "Dealer ID") @RequestParam(required = false) Long dealerId,
            @Parameter(description = "Brand ID") @RequestParam(required = false) int brandId,
            @Parameter(description = "Lọc kho của hãng") @RequestParam(required = false) Boolean isBrandWarehouse,
            @Parameter(description = "SL tồn tối thiểu") @RequestParam(required = false) Integer minAvailable,
            @Parameter(description = "SL tồn tối đa") @RequestParam(required = false) Integer maxAvailable,
            @Parameter(description = "Sắp xếp") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng") @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/inventory - productId: {}, dealerId: {}", productId, dealerId);

        InventoryFilterRequest filterRequest = InventoryFilterRequest.builder()
                .searchKeyword(searchKeyword)
                .productId(productId)
                .dealerId(dealerId)
                .brandId(brandId)
                .isBrandWarehouse(isBrandWarehouse)
                .minAvailable(minAvailable)
                .maxAvailable(maxAvailable)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<InventoryListResponse> inventories = inventoryService.getAllInventory(filterRequest);
        return ResponseEntity.ok(inventories);
    }

    /**
     * GET: Lấy kho hàng theo sản phẩm
     * Endpoint: GET /api/inventory/product/{productId}
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy kho hàng theo sản phẩm")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    public ResponseEntity<List<InventoryListResponse>> getInventoryByProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId
    ) {
        log.info("GET /api/inventory/product/{}", productId);
        List<InventoryListResponse> inventories = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(inventories);
    }

    /**
     * GET: Lấy kho hàng theo đại lý
     * Endpoint: GET /api/inventory/dealer/{dealerId}
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy kho hàng theo đại lý")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đại lý")
    })
    public ResponseEntity<List<InventoryListResponse>> getInventoryByDealer(
            @Parameter(description = "Dealer ID", required = true) @PathVariable Long dealerId
    ) {
        log.info("GET /api/inventory/dealer/{}", dealerId);
        List<InventoryListResponse> inventories = inventoryService.getInventoryByDealer(dealerId);
        return ResponseEntity.ok(inventories);
    }

    /**
     * GET: Lấy kho hàng của hãng (không thuộc đại lý nào)
     * Endpoint: GET /api/inventory/brand-warehouse
     */
    @GetMapping("/brand-warehouse")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy kho hàng của hãng (tổng)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<InventoryListResponse>> getBrandWarehouseInventory() {
        log.info("GET /api/inventory/brand-warehouse");
        List<InventoryListResponse> inventories = inventoryService.getBrandWarehouseInventory();
        return ResponseEntity.ok(inventories);
    }

    /**
     * GET: Lấy các kho hàng sắp hết hàng
     * Endpoint: GET /api/inventory/low-stock
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy các kho hàng sắp hết hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<InventoryListResponse>> getLowStockInventory(
            @Parameter(description = "Ngưỡng (mặc định 5)") @RequestParam(required = false) Integer threshold
    ) {
        log.info("GET /api/inventory/low-stock - threshold: {}", threshold);
        List<InventoryListResponse> inventories = inventoryService.getLowStockInventory(threshold);
        return ResponseEntity.ok(inventories);
    }

    /**
     * GET: Xem chi tiết kho hàng
     * Endpoint: GET /api/inventory/{inventoryId}
     */
    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Xem chi tiết kho hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<InventoryDetailResponse> getInventoryById(
            @Parameter(description = "Inventory ID", required = true) @PathVariable Long inventoryId
    ) {
        log.info("GET /api/inventory/{}", inventoryId);
        InventoryDetailResponse inventory = inventoryService.getInventoryById(inventoryId);
        return ResponseEntity.ok(inventory);
    }

    /**
     * GET: Tra cứu kho hàng theo sản phẩm và đại lý
     * Endpoint: GET /api/inventory/lookup
     */
    @GetMapping("/lookup")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tra cứu kho hàng theo sản phẩm và đại lý")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<InventoryDetailResponse> getInventoryByProductAndDealer(
            @Parameter(description = "Product ID", required = true) @RequestParam Long productId,
            @Parameter(description = "Dealer ID", required = true) @RequestParam Long dealerId
    ) {
        log.info("GET /api/inventory/lookup - productId: {}, dealerId: {}", productId, dealerId);
        InventoryDetailResponse inventory = inventoryService.getInventoryByProductAndDealer(productId, dealerId);
        return ResponseEntity.ok(inventory);
    }

    /**
     * POST: Tạo bản ghi kho hàng mới
     * Endpoint: POST /api/inventory
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tạo bản ghi kho hàng mới", description = "Tạo kho hàng mới (thường chỉ dành cho admin/manager)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo kho hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (vd: đã tồn tại, số lượng sai)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc đại lý")
    })
    public ResponseEntity<InventoryDetailResponse> createInventory(
            Authentication authentication,
            @Parameter(description = "Thông tin kho hàng", required = true)
            @Valid @RequestBody InventoryRequest request
    ) {
        log.info("POST /api/inventory - Product: {}", request.getProductId());
        InventoryDetailResponse createdInventory = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
    }

    /**
     * PUT: Cập nhật thông tin kho hàng
     * Endpoint: PUT /api/inventory/{inventoryId}
     */
    @PutMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật thông tin kho hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (vd: số lượng sai)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<InventoryDetailResponse> updateInventory(
            Authentication authentication,
            @Parameter(description = "Inventory ID", required = true) @PathVariable Long inventoryId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody InventoryRequest request
    ) {
        log.info("PUT /api/inventory/{}", inventoryId);
        InventoryDetailResponse updatedInventory = inventoryService.updateInventory(inventoryId, request);
        return ResponseEntity.ok(updatedInventory);
    }

    /**
     * PATCH: Điều chỉnh số lượng (nhập/xuất kho thủ công)
     * Endpoint: PATCH /api/inventory/{inventoryId}/adjust
     */
    @PatchMapping("/{inventoryId}/adjust")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Điều chỉnh số lượng (nhập/xuất thủ công)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Điều chỉnh thành công"),
            @ApiResponse(responseCode = "400", description = "Số lượng không hợp lệ (vd: < 0)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<InventoryDetailResponse> adjustQuantity(
            Authentication authentication,
            @Parameter(description = "Inventory ID", required = true) @PathVariable Long inventoryId,
            @Parameter(description = "Số lượng (dương để tăng, âm để giảm)", required = true) @RequestParam Integer quantity,
            @Parameter(description = "Lý do điều chỉnh", required = true) @RequestParam String reason
    ) {
        log.info("PATCH /api/inventory/{}/adjust - quantity: {}, reason: {}", inventoryId, quantity, reason);
        InventoryDetailResponse adjustedInventory = inventoryService.adjustQuantity(inventoryId, quantity, reason);
        return ResponseEntity.ok(adjustedInventory);
    }

    /**
     * PATCH: Đặt hàng (tạm giữ)
     * Endpoint: PATCH /api/inventory/{inventoryId}/reserve
     */
    @PatchMapping("/{inventoryId}/reserve")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Đặt hàng (tạm giữ) số lượng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạm giữ thành công"),
            @ApiResponse(responseCode = "400", description = "Không đủ hàng có sẵn"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<InventoryDetailResponse> reserveInventory(
            Authentication authentication,
            @Parameter(description = "Inventory ID", required = true) @PathVariable Long inventoryId,
            @Parameter(description = "Số lượng cần tạm giữ", required = true) @RequestParam Integer quantity
    ) {
        log.info("PATCH /api/inventory/{}/reserve - quantity: {}", inventoryId, quantity);
        InventoryDetailResponse reservedInventory = inventoryService.reserveInventory(inventoryId, quantity);
        return ResponseEntity.ok(reservedInventory);
    }

    /**
     * PATCH: Hủy đặt hàng (nhả tạm giữ)
     * Endpoint: PATCH /api/inventory/{inventoryId}/release
     */
    @PatchMapping("/{inventoryId}/release")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Hủy đặt hàng (nhả tạm giữ) số lượng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nhả thành công"),
            @ApiResponse(responseCode = "400", description = "Số lượng nhả không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<InventoryDetailResponse> releaseReservedInventory(
            Authentication authentication,
            @Parameter(description = "Inventory ID", required = true) @PathVariable Long inventoryId,
            @Parameter(description = "Số lượng cần nhả", required = true) @RequestParam Integer quantity
    ) {
        log.info("PATCH /api/inventory/{}/release - quantity: {}", inventoryId, quantity);
        InventoryDetailResponse releasedInventory = inventoryService.releaseReservedInventory(inventoryId, quantity);
        return ResponseEntity.ok(releasedInventory);
    }

    /**
     * POST: Chuyển kho
     * Endpoint: POST /api/inventory/{fromInventoryId}/transfer
     */
    @PostMapping("/{fromInventoryId}/transfer")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Chuyển kho (từ kho này sang đại lý khác)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chuyển kho thành công (trả về kho NGUỒN)"),
            @ApiResponse(responseCode = "400", description = "Không đủ hàng để chuyển"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho nguồn hoặc đại lý đích")
    })
    public ResponseEntity<InventoryDetailResponse> transferInventory(
            Authentication authentication,
            @Parameter(description = "ID kho nguồn", required = true) @PathVariable Long fromInventoryId,
            @Parameter(description = "ID đại lý đích", required = true) @RequestParam Long toDealerId,
            @Parameter(description = "Số lượng chuyển", required = true) @RequestParam Integer quantity
    ) {
        log.info("POST /api/inventory/{}/transfer - toDealer: {}, quantity: {}", fromInventoryId, toDealerId, quantity);
        InventoryDetailResponse sourceInventory = inventoryService.transferInventory(fromInventoryId, toDealerId, quantity);
        return ResponseEntity.ok(sourceInventory);
    }

    /**
     * DELETE: Xóa bản ghi kho hàng
     * Endpoint: DELETE /api/inventory/{inventoryId}
     */
    @DeleteMapping("/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa bản ghi kho hàng (chỉ ADMIN)", description = "Chỉ xóa được nếu tổng số lượng = 0")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa do vẫn còn hàng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kho hàng")
    })
    public ResponseEntity<Void> deleteInventory(
            Authentication authentication,
            @Parameter(description = "Inventory ID", required = true) @PathVariable Long inventoryId
    ) {
        log.info("DELETE /api/inventory/{}", inventoryId);
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET: Thống kê kho hàng
     * Endpoint: GET /api/inventory/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Thống kê kho hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    })
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        log.info("GET /api/inventory/statistics");
        Map<String, Object> statistics = inventoryService.getInventoryStatistics();
        return ResponseEntity.ok(statistics);
    }
}
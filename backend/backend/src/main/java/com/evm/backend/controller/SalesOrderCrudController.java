package com.evm.backend.controller;

import com.evm.backend.dto.request.AssignVehicleRequest;
import com.evm.backend.dto.request.SalesOrderFilterRequest;
import com.evm.backend.dto.request.SalesOrderRequest;
import com.evm.backend.dto.response.SalesOrderDetailResponse;
import com.evm.backend.dto.response.SalesOrderListResponse;
import com.evm.backend.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Order Management", description = "APIs quản lý đơn hàng xe điện")
public class SalesOrderCrudController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách đơn hàng")
    public ResponseEntity<Page<SalesOrderListResponse>> getAllOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long salesPersonId,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        SalesOrderFilterRequest req = SalesOrderFilterRequest.builder()
                .customerId(customerId)
                .salesPersonId(salesPersonId)
                .vehicleId(vehicleId)
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(salesOrderService.getAllOrders(req));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy đơn hàng gần đây (7 ngày)")
    public ResponseEntity<List<SalesOrderListResponse>> getRecentOrders() {
        return ResponseEntity.ok(salesOrderService.getRecentOrders());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy đơn hàng đang chờ xử lý")
    public ResponseEntity<List<SalesOrderListResponse>> getPendingOrders() {
        return ResponseEntity.ok(salesOrderService.getPendingOrders());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xem chi tiết đơn hàng")
    public ResponseEntity<SalesOrderDetailResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(salesOrderService.getOrderById(orderId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tạo đơn hàng mới")
    public ResponseEntity<SalesOrderDetailResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody SalesOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salesOrderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật đơn hàng")
    public ResponseEntity<SalesOrderDetailResponse> updateOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody SalesOrderRequest request
    ) {
        return ResponseEntity.ok(salesOrderService.updateOrder(orderId, request));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Hủy đơn hàng")
    public ResponseEntity<Void> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        salesOrderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng")
    public ResponseEntity<SalesOrderDetailResponse> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(salesOrderService.updateOrderStatus(orderId, status));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa đơn hàng")
    public ResponseEntity<Void> deleteOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        salesOrderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/monthly-sales")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Báo cáo doanh số theo tháng")
    public ResponseEntity<List<SalesOrderListResponse>> getMonthlySales(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(salesOrderService.getMonthlySales(year, month));
    }

    @PutMapping("/{orderId}/assign-vehicle")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Gán xe cho đơn hàng",
            description = "Gán xe cụ thể (VIN) cho đơn hàng đang ở trạng thái PENDING. " +
                    "Sau khi gán xe thành công, trạng thái đơn hàng chuyển sang CONFIRMED."
    )
    public ResponseEntity<SalesOrderDetailResponse> assignVehicle(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody AssignVehicleRequest request
    ) {
        log.info("REST request to assign vehicle {} to order {}",
                request.getVehicleId(), orderId);

        SalesOrderDetailResponse response = salesOrderService.assignVehicle(orderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}/vehicle")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Hủy gán xe",
            description = "Hủy gán xe cho đơn hàng. Trạng thái đơn hàng chuyển về PENDING, " +
                    "xe chuyển về AVAILABLE."
    )
    public ResponseEntity<SalesOrderDetailResponse> unassignVehicle(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        log.info("REST request to unassign vehicle from order {}", orderId);

        SalesOrderDetailResponse response = salesOrderService.unassignVehicle(orderId);
        return ResponseEntity.ok(response);
    }
}
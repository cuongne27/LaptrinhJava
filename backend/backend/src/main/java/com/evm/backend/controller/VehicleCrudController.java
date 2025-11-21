package com.evm.backend.controller;

import com.evm.backend.dto.request.VehicleFilterRequest;
import com.evm.backend.dto.request.VehicleRequest;
import com.evm.backend.dto.response.VehicleDetailResponse;
import com.evm.backend.dto.response.VehicleListResponse;
import com.evm.backend.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/vehicles") // <<< MODULE: QUẢN LÝ XE (INVENTORY)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Management", description = "APIs quản lý xe (inventory)")
public class VehicleCrudController {

    private final VehicleService vehicleService;

    // <<< CHỨC NĂNG: LẤY DANH SÁCH XE (CÓ FILTER VÀ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/vehicles
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách xe", description = "Lấy danh sách xe với filter và phân trang")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<VehicleListResponse>> getAllVehicles(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate manufactureFromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate manufactureToDate,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        VehicleFilterRequest filterRequest = VehicleFilterRequest.builder()
                .productId(productId).dealerId(dealerId).color(color).status(status)
                .manufactureFromDate(manufactureFromDate).manufactureToDate(manufactureToDate)
                .searchKeyword(searchKeyword).sortBy(sortBy).page(page).size(size).build();

        return ResponseEntity.ok(vehicleService.getAllVehicles(filterRequest));
    }

    // <<< CHỨC NĂNG: LẤY DANH SÁCH XE THEO PRODUCT
    // <<< ĐẦU API: GET /api/vehicles/product/{productId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy xe theo product")
    public ResponseEntity<List<VehicleListResponse>> getVehiclesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByProduct(productId));
    }

    // <<< CHỨC NĂNG: LẤY DANH SÁCH XE THEO DEALER
    // <<< ĐẦU API: GET /api/vehicles/dealer/{dealerId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy xe theo dealer")
    public ResponseEntity<List<VehicleListResponse>> getVehiclesByDealer(@PathVariable Long dealerId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByDealer(dealerId));
    }

    // <<< CHỨC NĂNG: LẤY DANH SÁCH XE ĐANG AVAILABLE CỦA DEALER
    // <<< ĐẦU API: GET /api/vehicles/dealer/{dealerId}/available
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/dealer/{dealerId}/available")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy xe available của dealer")
    public ResponseEntity<List<VehicleListResponse>> getAvailableVehiclesByDealer(@PathVariable Long dealerId) {
        return ResponseEntity.ok(vehicleService.getAvailableVehiclesByDealer(dealerId));
    }

    // <<< CHỨC NĂNG: XEM CHI TIẾT XE THEO ID
    // <<< ĐẦU API: GET /api/vehicles/{vehicleId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Xem chi tiết xe")
    public ResponseEntity<VehicleDetailResponse> getVehicleById(@PathVariable String vehicleId) {
        return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId));
    }

    // <<< CHỨC NĂNG: TÌM XE THEO VIN
    // <<< ĐẦU API: GET /api/vehicles/vin/{vin}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/vin/{vin}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tìm xe theo VIN")
    public ResponseEntity<VehicleDetailResponse> getVehicleByVin(@PathVariable String vin) {
        return ResponseEntity.ok(vehicleService.getVehicleByVin(vin));
    }

    // <<< CHỨC NĂNG: TẠO XE MỚI
    // <<< ĐẦU API: POST /api/vehicles
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tạo xe mới")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo xe thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc ID/VIN đã tồn tại")
    })
    public ResponseEntity<VehicleDetailResponse> createVehicle(
            Authentication authentication,
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(request));
    }

    // <<< CHỨC NĂNG: CẬP NHẬT THÔNG TIN XE
    // <<< ĐẦU API: PUT /api/vehicles/{vehicleId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PutMapping("/{vehicleId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật xe")
    public ResponseEntity<VehicleDetailResponse> updateVehicle(
            Authentication authentication,
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, request));
    }

    // <<< CHỨC NĂNG: XÓA XE
    // <<< ĐẦU API: DELETE /api/vehicles/{vehicleId}
    // <<< VAI TRÒ: ADMIN
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa xe")
    public ResponseEntity<Void> deleteVehicle(
            Authentication authentication,
            @PathVariable String vehicleId
    ) {
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }
}
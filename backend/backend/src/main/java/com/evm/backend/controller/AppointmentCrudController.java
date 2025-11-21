package com.evm.backend.controller;

import com.evm.backend.dto.request.AppointmentFilterRequest;
import com.evm.backend.dto.request.AppointmentRequest;
import com.evm.backend.dto.response.AppointmentDetailResponse;
import com.evm.backend.dto.response.AppointmentListResponse;
import com.evm.backend.service.AppointmentService;
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
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments") // <<< MODULE: QUẢN LÝ LỊCH HẸN (APPOINTMENT MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointment Management", description = "APIs quản lý lịch hẹn")
public class AppointmentCrudController {

    private final AppointmentService appointmentService;

    // <<< CHỨC NĂNG: LẤY DANH SÁCH LỊCH HẸN (CÓ FILTER/PHÂN TRANG)
    // <<< ĐẦU API: GET /api/appointments
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách lịch hẹn")
    public ResponseEntity<Page<AppointmentListResponse>> getAllAppointments(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long staffUserId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        AppointmentFilterRequest req = AppointmentFilterRequest.builder()
                .customerId(customerId).staffUserId(staffUserId).productId(productId)
                .dealerId(dealerId).status(status).fromDate(fromDate).toDate(toDate)
                .sortBy(sortBy).page(page).size(size).build();
        return ResponseEntity.ok(appointmentService.getAllAppointments(req));
    }

    // <<< CHỨC NĂNG: LẤY LỊCH HẸN SẮP TỚI
    // <<< ĐẦU API: GET /api/appointments/upcoming
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy lịch hẹn sắp tới")
    public ResponseEntity<List<AppointmentListResponse>> getUpcomingAppointments() {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointments());
    }

    // <<< CHỨC NĂNG: LẤY LỊCH HẸN HÔM NAY
    // <<< ĐẦU API: GET /api/appointments/today
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy lịch hẹn hôm nay")
    public ResponseEntity<List<AppointmentListResponse>> getTodayAppointments() {
        return ResponseEntity.ok(appointmentService.getTodayAppointments());
    }

    // <<< CHỨC NĂNG: XEM CHI TIẾT LỊCH HẸN THEO ID
    // <<< ĐẦU API: GET /api/appointments/{appointmentId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN, CUSTOMER
    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xem chi tiết lịch hẹn")
    public ResponseEntity<AppointmentDetailResponse> getAppointmentById(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    // <<< CHỨC NĂNG: TẠO LỊCH HẸN MỚI
    // <<< ĐẦU API: POST /api/appointments
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tạo lịch hẹn mới")
    public ResponseEntity<AppointmentDetailResponse> createAppointment(
            Authentication authentication,
            @Valid @RequestBody AppointmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(request));
    }

    // <<< CHỨC NĂNG: CẬP NHẬT LỊCH HẸN
    // <<< ĐẦU API: PUT /api/appointments/{appointmentId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật lịch hẹn")
    public ResponseEntity<AppointmentDetailResponse> updateAppointment(
            Authentication authentication,
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.updateAppointment(appointmentId, request));
    }

    // <<< CHỨC NĂNG: HỦY LỊCH HẸN
    // <<< ĐẦU API: PATCH /api/appointments/{appointmentId}/cancel
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN, CUSTOMER
    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Hủy lịch hẹn")
    public ResponseEntity<Void> cancelAppointment(
            Authentication authentication,
            @PathVariable Long appointmentId
    ) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    // <<< CHỨC NĂNG: XÓA LỊCH HẸN (ADMIN ONLY)
    // <<< ĐẦU API: DELETE /api/appointments/{appointmentId}
    // <<< VAI TRÒ: ADMIN
    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa lịch hẹn")
    public ResponseEntity<Void> deleteAppointment(
            Authentication authentication,
            @PathVariable Long appointmentId
    ) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
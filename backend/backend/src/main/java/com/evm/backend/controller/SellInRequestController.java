package com.evm.backend.controller;

import com.evm.backend.dto.request.SellInRequestFilterRequest;
import com.evm.backend.dto.request.SellInRequestRequest;
import com.evm.backend.dto.response.SellInRequestResponse;
import com.evm.backend.service.SellInRequestService;
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
@RequestMapping("/api/sell-in-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sell-in Request Management", description = "APIs quản lý đặt xe từ Hãng")
public class SellInRequestController {

    private final SellInRequestService sellInRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_MANAGER', 'DEALER_STAFF', 'ADMIN')")
    @Operation(summary = "Tạo yêu cầu đặt xe từ Hãng",
            description = "Dealer tạo yêu cầu nhập xe, chọn mẫu xe, số lượng, màu sắc")
    public ResponseEntity<SellInRequestResponse> createRequest(
            Authentication authentication,
            @Valid @RequestBody SellInRequestRequest request
    ) {
        log.info("REST request to create sell-in request");
        SellInRequestResponse response = sellInRequestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_MANAGER', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách yêu cầu đặt xe với filter")
    public ResponseEntity<Page<SellInRequestResponse>> getAllRequests(
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("REST request to get all sell-in requests");

        SellInRequestFilterRequest filterRequest = SellInRequestFilterRequest.builder()
                .dealerId(dealerId)
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<SellInRequestResponse> response = sellInRequestService.getAllRequests(filterRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEALER_MANAGER', 'DEALER_STAFF', 'ADMIN')")
    @Operation(summary = "Cập nhật yêu cầu đặt xe",
            description = "Chỉ có thể cập nhật yêu cầu ở trạng thái PENDING")
    public ResponseEntity<SellInRequestResponse> updateRequest(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SellInRequestRequest request
    ) {
        log.info("REST request to update sell-in request: {}", id);
        SellInRequestResponse response = sellInRequestService.updateRequest(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa yêu cầu đặt xe")
    public ResponseEntity<Void> deleteRequest(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to delete sell-in request: {}", id);
        sellInRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Phê duyệt yêu cầu đặt xe",
            description = "Hãng phê duyệt yêu cầu từ Dealer")
    public ResponseEntity<SellInRequestResponse> approveRequest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) String approvalNotes
    ) {
        log.info("REST request to approve sell-in request: {}", id);
        // TODO: Get current user ID from authentication
        Long approvedBy = 1L; // Placeholder
        SellInRequestResponse response = sellInRequestService.approveRequest(id, approvalNotes, approvedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Từ chối yêu cầu đặt xe")
    public ResponseEntity<SellInRequestResponse> rejectRequest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) String rejectionNotes
    ) {
        log.info("REST request to reject sell-in request: {}", id);
        Long rejectedBy = 1L; // Placeholder
        SellInRequestResponse response = sellInRequestService.rejectRequest(id, rejectionNotes, rejectedBy);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật trạng thái yêu cầu")
    public ResponseEntity<SellInRequestResponse> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.info("REST request to update sell-in request status: {} -> {}", id, status);
        SellInRequestResponse response = sellInRequestService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/mark-in-transit")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Đánh dấu đang vận chuyển",
            description = "Đánh dấu xe đang được vận chuyển đến Dealer")
    public ResponseEntity<SellInRequestResponse> markAsInTransit(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to mark sell-in request as in transit: {}", id);
        SellInRequestResponse response = sellInRequestService.markAsInTransit(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/mark-delivered")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'DEALER_MANAGER', 'ADMIN')")
    @Operation(summary = "Đánh dấu đã giao xe",
            description = "Xác nhận đã giao xe đến Dealer, cập nhật tồn kho")
    public ResponseEntity<SellInRequestResponse> markAsDelivered(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to mark sell-in request as delivered: {}", id);
        SellInRequestResponse response = sellInRequestService.markAsDelivered(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('DEALER_MANAGER', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy yêu cầu của Dealer")
    public ResponseEntity<List<SellInRequestResponse>> getRequestsByDealer(
            @PathVariable Long dealerId
    ) {
        log.info("REST request to get sell-in requests by dealer: {}", dealerId);
        List<SellInRequestResponse> response = sellInRequestService.getRequestsByDealer(dealerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách yêu cầu chờ duyệt")
    public ResponseEntity<List<SellInRequestResponse>> getPendingRequests() {
        log.info("REST request to get pending sell-in requests");
        List<SellInRequestResponse> response = sellInRequestService.getPendingRequests();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming-deliveries")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'DEALER_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách giao xe sắp tới (7 ngày)",
            description = "Danh sách các đơn sẽ được giao trong 7 ngày tới")
    public ResponseEntity<List<SellInRequestResponse>> getUpcomingDeliveries() {
        log.info("REST request to get upcoming deliveries");
        List<SellInRequestResponse> response = sellInRequestService.getUpcomingDeliveries();
        return ResponseEntity.ok(response);
    }
}
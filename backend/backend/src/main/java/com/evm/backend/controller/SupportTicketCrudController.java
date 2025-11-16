package com.evm.backend.controller;

import com.evm.backend.dto.request.SupportTicketFilterRequest;
import com.evm.backend.dto.request.SupportTicketRequest;
import com.evm.backend.dto.response.SupportTicketDetailResponse;
import com.evm.backend.dto.response.SupportTicketListResponse;
import com.evm.backend.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * CRUD operations for Support Ticket entity
 */
@RestController
@RequestMapping("/api/support-tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Support Ticket Management", description = "APIs quản lý tickets hỗ trợ khách hàng")
public class SupportTicketCrudController {

    private final SupportTicketService supportTicketService;

    /**
     * GET: Lấy danh sách tickets với filter
     * Endpoint: GET /api/support-tickets
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách tickets", description = "Lấy danh sách tickets với filter và phân trang")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<SupportTicketListResponse>> getAllTickets(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String searchKeyword,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) String status,
            @Parameter(description = "Độ ưu tiên") @RequestParam(required = false) String priority,
            @Parameter(description = "Danh mục") @RequestParam(required = false) String category,
            @Parameter(description = "Customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Assigned User ID") @RequestParam(required = false) Long assignedUserId,
            @Parameter(description = "Sales Order ID") @RequestParam(required = false) Long salesOrderId,
            @Parameter(description = "Vehicle ID") @RequestParam(required = false) String vehicleId,
            @Parameter(description = "Từ ngày") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @Parameter(description = "Đến ngày") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @Parameter(description = "Sắp xếp") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng") @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/support-tickets - status: {}, priority: {}", status, priority);

        SupportTicketFilterRequest filterRequest = SupportTicketFilterRequest.builder()
                .searchKeyword(searchKeyword)
                .status(status)
                .priority(priority)
                .category(category)
                .customerId(customerId)
                .assignedUserId(assignedUserId)
                .salesOrderId(salesOrderId)
                .vehicleId(vehicleId)
                .fromDate(fromDate)
                .toDate(toDate)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<SupportTicketListResponse> tickets = supportTicketService.getAllTickets(filterRequest);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy tickets theo customer
     * Endpoint: GET /api/support-tickets/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Lấy tickets theo khách hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getTicketsByCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId
    ) {
        log.info("GET /api/support-tickets/customer/{}", customerId);
        List<SupportTicketListResponse> tickets = supportTicketService.getTicketsByCustomer(customerId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy tickets theo assigned user
     * Endpoint: GET /api/support-tickets/assigned/{userId}
     */
    @GetMapping("/assigned/{userId}")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy tickets theo nhân viên được assign")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getTicketsByAssignedUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId
    ) {
        log.info("GET /api/support-tickets/assigned/{}", userId);
        List<SupportTicketListResponse> tickets = supportTicketService.getTicketsByAssignedUser(userId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy tickets theo sales order
     * Endpoint: GET /api/support-tickets/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy tickets theo đơn hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getTicketsBySalesOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId
    ) {
        log.info("GET /api/support-tickets/order/{}", orderId);
        List<SupportTicketListResponse> tickets = supportTicketService.getTicketsBySalesOrder(orderId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy tickets theo vehicle
     * Endpoint: GET /api/support-tickets/vehicle/{vehicleId}
     */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy tickets theo xe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getTicketsByVehicle(
            @Parameter(description = "Vehicle ID", required = true) @PathVariable String vehicleId
    ) {
        log.info("GET /api/support-tickets/vehicle/{}", vehicleId);
        List<SupportTicketListResponse> tickets = supportTicketService.getTicketsByVehicle(vehicleId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy tickets đang mở
     * Endpoint: GET /api/support-tickets/open
     */
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy tickets đang mở")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getOpenTickets() {
        log.info("GET /api/support-tickets/open");
        List<SupportTicketListResponse> tickets = supportTicketService.getOpenTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy tickets pending (chưa assign)
     * Endpoint: GET /api/support-tickets/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy tickets chưa assign")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getPendingTickets() {
        log.info("GET /api/support-tickets/pending");
        List<SupportTicketListResponse> tickets = supportTicketService.getPendingTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Lấy my tickets (của user hiện tại)
     * Endpoint: GET /api/support-tickets/my-tickets
     */
    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF')")
    @Operation(summary = "Lấy tickets của tôi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<SupportTicketListResponse>> getMyTickets(
            Authentication authentication,
            @Parameter(description = "User ID") @RequestParam Long userId
    ) {
        log.info("GET /api/support-tickets/my-tickets - userId: {}", userId);
        List<SupportTicketListResponse> tickets = supportTicketService.getMyTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET: Xem chi tiết ticket
     * Endpoint: GET /api/support-tickets/{ticketId}
     */
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xem chi tiết ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket")
    })
    public ResponseEntity<SupportTicketDetailResponse> getTicketById(
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId
    ) {
        log.info("GET /api/support-tickets/{}", ticketId);
        SupportTicketDetailResponse ticket = supportTicketService.getTicketById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    /**
     * POST: Tạo ticket mới
     * Endpoint: POST /api/support-tickets
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'CUSTOMER', 'ADMIN')")
    @Operation(summary = "Tạo ticket mới", description = "Tạo ticket hỗ trợ mới")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo ticket thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy customer/order/vehicle")
    })
    public ResponseEntity<SupportTicketDetailResponse> createTicket(
            Authentication authentication,
            @Parameter(description = "Thông tin ticket", required = true)
            @Valid @RequestBody SupportTicketRequest request
    ) {
        log.info("POST /api/support-tickets - Customer: {}", request.getCustomerId());
        SupportTicketDetailResponse createdTicket = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    /**
     * PUT: Cập nhật ticket
     * Endpoint: PUT /api/support-tickets/{ticketId}
     */
    @PutMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket")
    })
    public ResponseEntity<SupportTicketDetailResponse> updateTicket(
            Authentication authentication,
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody SupportTicketRequest request
    ) {
        log.info("PUT /api/support-tickets/{}", ticketId);
        SupportTicketDetailResponse updatedTicket = supportTicketService.updateTicket(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    /**
     * PATCH: Assign ticket cho user
     * Endpoint: PATCH /api/support-tickets/{ticketId}/assign
     */
    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Assign ticket cho nhân viên")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assign thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket hoặc user")
    })
    public ResponseEntity<SupportTicketDetailResponse> assignTicket(
            Authentication authentication,
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId,
            @Parameter(description = "User ID", required = true) @RequestParam Long userId
    ) {
        log.info("PATCH /api/support-tickets/{}/assign - userId: {}", ticketId, userId);
        SupportTicketDetailResponse assignedTicket = supportTicketService.assignTicket(ticketId, userId);
        return ResponseEntity.ok(assignedTicket);
    }

    /**
     * PATCH: Cập nhật trạng thái ticket
     * Endpoint: PATCH /api/support-tickets/{ticketId}/status
     */
    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật trạng thái ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket")
    })
    public ResponseEntity<SupportTicketDetailResponse> updateTicketStatus(
            Authentication authentication,
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId,
            @Parameter(description = "Status mới", required = true) @RequestParam String status
    ) {
        log.info("PATCH /api/support-tickets/{}/status - status: {}", ticketId, status);
        SupportTicketDetailResponse updatedTicket = supportTicketService.updateTicketStatus(ticketId, status);
        return ResponseEntity.ok(updatedTicket);
    }

    /**
     * PATCH: Đóng ticket
     * Endpoint: PATCH /api/support-tickets/{ticketId}/close
     */
    @PatchMapping("/{ticketId}/close")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Đóng ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đóng ticket thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket")
    })
    public ResponseEntity<SupportTicketDetailResponse> closeTicket(
            Authentication authentication,
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId
    ) {
        log.info("PATCH /api/support-tickets/{}/close", ticketId);
        SupportTicketDetailResponse closedTicket = supportTicketService.closeTicket(ticketId);
        return ResponseEntity.ok(closedTicket);
    }

    /**
     * PATCH: Mở lại ticket
     * Endpoint: PATCH /api/support-tickets/{ticketId}/reopen
     */
    @PatchMapping("/{ticketId}/reopen")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Mở lại ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mở lại ticket thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket")
    })
    public ResponseEntity<SupportTicketDetailResponse> reopenTicket(
            Authentication authentication,
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId
    ) {
        log.info("PATCH /api/support-tickets/{}/reopen", ticketId);
        SupportTicketDetailResponse reopenedTicket = supportTicketService.reopenTicket(ticketId);
        return ResponseEntity.ok(reopenedTicket);
    }

    /**
     * DELETE: Xóa ticket
     * Endpoint: DELETE /api/support-tickets/{ticketId}
     */
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa ticket (chỉ ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ticket")
    })
    public ResponseEntity<Void> deleteTicket(
            Authentication authentication,
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId
    ) {
        log.info("DELETE /api/support-tickets/{}", ticketId);
        supportTicketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET: Thống kê tickets
     * Endpoint: GET /api/support-tickets/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPPORT_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Thống kê tickets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    })
    public ResponseEntity<Map<String, Long>> getTicketStatistics() {
        log.info("GET /api/support-tickets/statistics");
        Map<String, Long> statistics = supportTicketService.getTicketStatistics();
        return ResponseEntity.ok(statistics);
    }
}
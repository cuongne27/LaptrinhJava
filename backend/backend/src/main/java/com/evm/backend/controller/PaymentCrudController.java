package com.evm.backend.controller;

import com.evm.backend.dto.request.PaymentFilterRequest;
import com.evm.backend.dto.request.PaymentRequest;
import com.evm.backend.dto.response.PaymentDetailResponse;
import com.evm.backend.dto.response.PaymentListResponse;
import com.evm.backend.service.PaymentService;
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

import java.time.LocalDate;
import java.util.List;

/**
 * CRUD operations for Payment entity
 */
@RestController
@RequestMapping("/api/payments") // <<< MODULE: QUẢN LÝ THANH TOÁN (PAYMENT MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs quản lý thanh toán đơn hàng xe điện")
public class PaymentCrudController {

    private final PaymentService paymentService;

    /**
     * GET: Lấy danh sách payments với filter
     * Endpoint: GET /api/payments
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH THANH TOÁN (CÓ FILTER VÀ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/payments
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy danh sách thanh toán",
            description = "Lấy danh sách thanh toán với filter và phân trang"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<PaymentListResponse>> getAllPayments(
            @Parameter(description = "Order ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "Customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Phương thức thanh toán") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) String status,
            @Parameter(description = "Loại thanh toán") @RequestParam(required = false) String paymentType,
            @Parameter(description = "Từ ngày") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Đến ngày") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Mã tham chiếu") @RequestParam(required = false) String referenceNumber,
            @Parameter(description = "Sắp xếp") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng") @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/payments - orderId: {}, status: {}", orderId, status);

        PaymentFilterRequest filterRequest = PaymentFilterRequest.builder()
                .orderId(orderId)
                .customerId(customerId)
                .paymentMethod(paymentMethod)
                .status(status)
                .paymentType(paymentType)
                .fromDate(fromDate)
                .toDate(toDate)
                .referenceNumber(referenceNumber)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<PaymentListResponse> payments = paymentService.getAllPayments(filterRequest);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET: Lấy payments theo order ID
     * Endpoint: GET /api/payments/order/{orderId}
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH THANH TOÁN THEO ĐƠN HÀNG (ORDER ID)
    // <<< ĐẦU API: GET /api/payments/order/{orderId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN, CUSTOMER
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Lấy danh sách thanh toán theo đơn hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    public ResponseEntity<List<PaymentListResponse>> getPaymentsByOrderId(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId
    ) {
        log.info("GET /api/payments/order/{}", orderId);
        List<PaymentListResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET: Lấy payments theo customer ID
     * Endpoint: GET /api/payments/customer/{customerId}
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH THANH TOÁN THEO KHÁCH HÀNG
    // <<< ĐẦU API: GET /api/payments/customer/{customerId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách thanh toán theo khách hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    public ResponseEntity<List<PaymentListResponse>> getPaymentsByCustomerId(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId
    ) {
        log.info("GET /api/payments/customer/{}", customerId);
        List<PaymentListResponse> payments = paymentService.getPaymentsByCustomerId(customerId);
        return ResponseEntity.ok(payments);
    }

    /**
     * GET: Lấy payments đang chờ xử lý
     * Endpoint: GET /api/payments/pending
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH THANH TOÁN ĐANG CHỜ XỬ LÝ (PENDING)
    // <<< ĐẦU API: GET /api/payments/pending
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách thanh toán đang chờ xử lý")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<PaymentListResponse>> getPendingPayments() {
        log.info("GET /api/payments/pending");
        List<PaymentListResponse> payments = paymentService.getPendingPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * GET: Xem chi tiết payment
     * Endpoint: GET /api/payments/{paymentId}
     */
    // <<< CHỨC NĂNG: XEM CHI TIẾT THANH TOÁN
    // <<< ĐẦU API: GET /api/payments/{paymentId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN, CUSTOMER
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xem chi tiết thanh toán")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán")
    })
    public ResponseEntity<PaymentDetailResponse> getPaymentById(
            @Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId
    ) {
        log.info("GET /api/payments/{}", paymentId);
        PaymentDetailResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * GET: Tìm payment theo reference number
     * Endpoint: GET /api/payments/reference/{referenceNumber}
     */
    // <<< CHỨC NĂNG: TÌM THANH TOÁN THEO MÃ THAM CHIẾU (REFERENCE NUMBER)
    // <<< ĐẦU API: GET /api/payments/reference/{referenceNumber}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/reference/{referenceNumber}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tìm thanh toán theo mã tham chiếu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy thanh toán"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán")
    })
    public ResponseEntity<PaymentDetailResponse> getPaymentByReferenceNumber(
            @Parameter(description = "Reference Number", required = true) @PathVariable String referenceNumber
    ) {
        log.info("GET /api/payments/reference/{}", referenceNumber);
        PaymentDetailResponse payment = paymentService.getPaymentByReferenceNumber(referenceNumber);
        return ResponseEntity.ok(payment);
    }

    /**
     * POST: Tạo payment mới
     * Endpoint: POST /api/payments
     */
    // <<< CHỨC NĂNG: TẠO BẢN GHI THANH TOÁN MỚI
    // <<< ĐẦU API: POST /api/payments
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tạo thanh toán mới")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo thanh toán thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    public ResponseEntity<PaymentDetailResponse> createPayment(
            Authentication authentication,
            @Parameter(description = "Thông tin thanh toán", required = true)
            @Valid @RequestBody PaymentRequest request
    ) {
        log.info("POST /api/payments - Order ID: {}, Amount: {}", request.getOrderId(), request.getAmount());
        PaymentDetailResponse createdPayment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    /**
     * PUT: Cập nhật payment
     * Endpoint: PUT /api/payments/{paymentId}
     */
    // <<< CHỨC NĂNG: CẬP NHẬT THÔNG TIN THANH TOÁN
    // <<< ĐẦU API: PUT /api/payments/{paymentId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PutMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật thanh toán")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán")
    })
    public ResponseEntity<PaymentDetailResponse> updatePayment(
            Authentication authentication,
            @Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
            @Parameter(description = "Thông tin thanh toán", required = true)
            @Valid @RequestBody PaymentRequest request
    ) {
        log.info("PUT /api/payments/{}", paymentId);
        PaymentDetailResponse updatedPayment = paymentService.updatePayment(paymentId, request);
        return ResponseEntity.ok(updatedPayment);
    }

    /**
     * PATCH: Xác nhận thanh toán
     * Endpoint: PATCH /api/payments/{paymentId}/confirm
     */
    // <<< CHỨC NĂNG: XÁC NHẬN THANH TOÁN (CHUYỂN TRẠNG THÁI)
    // <<< ĐẦU API: PATCH /api/payments/{paymentId}/confirm
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @PatchMapping("/{paymentId}/confirm")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Xác nhận thanh toán")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán"),
            @ApiResponse(responseCode = "400", description = "Không thể xác nhận thanh toán")
    })
    public ResponseEntity<PaymentDetailResponse> confirmPayment(
            Authentication authentication,
            @Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId
    ) {
        log.info("PATCH /api/payments/{}/confirm", paymentId);
        PaymentDetailResponse confirmedPayment = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(confirmedPayment);
    }

    /**
     * PATCH: Hoàn tiền
     * Endpoint: PATCH /api/payments/{paymentId}/refund
     */
    // <<< CHỨC NĂNG: HOÀN TIỀN THANH TOÁN
    // <<< ĐẦU API: PATCH /api/payments/{paymentId}/refund
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @PatchMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Hoàn tiền thanh toán")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoàn tiền thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán"),
            @ApiResponse(responseCode = "400", description = "Không thể hoàn tiền")
    })
    public ResponseEntity<PaymentDetailResponse> refundPayment(
            Authentication authentication,
            @Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId,
            @Parameter(description = "Lý do hoàn tiền") @RequestParam(required = false) String reason
    ) {
        log.info("PATCH /api/payments/{}/refund - Reason: {}", paymentId, reason);
        PaymentDetailResponse refundedPayment = paymentService.refundPayment(paymentId, reason);
        return ResponseEntity.ok(refundedPayment);
    }

    /**
     * DELETE: Xóa payment
     * Endpoint: DELETE /api/payments/{paymentId}
     */
    // <<< CHỨC NĂNG: XÓA BẢN GHI THANH TOÁN
    // <<< ĐẦU API: DELETE /api/payments/{paymentId}
    // <<< VAI TRÒ: ADMIN
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa thanh toán (chỉ ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa thanh toán đã hoàn tất")
    })
    public ResponseEntity<Void> deletePayment(
            Authentication authentication,
            @Parameter(description = "Payment ID", required = true) @PathVariable Long paymentId
    ) {
        log.info("DELETE /api/payments/{}", paymentId);
        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}
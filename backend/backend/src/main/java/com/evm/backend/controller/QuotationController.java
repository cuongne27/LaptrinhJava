package com.evm.backend.controller;

import com.evm.backend.dto.request.QuotationRequest;
import com.evm.backend.dto.response.QuotationResponse;
import com.evm.backend.service.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quotation Management", description = "APIs quản lý báo giá")
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tạo báo giá mới", description = "Tạo báo giá cho khách hàng với đầy đủ chi tiết giá")
    public ResponseEntity<QuotationResponse> createQuotation(
            Authentication authentication,
            @Valid @RequestBody QuotationRequest request
    ) {
        log.info("REST request to create quotation");
        QuotationResponse response = quotationService.createQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xem chi tiết báo giá")
    public ResponseEntity<QuotationResponse> getQuotationById(@PathVariable Long id) {
        log.info("REST request to get quotation: {}", id);
        QuotationResponse response = quotationService.getQuotationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{quotationNumber}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xem báo giá theo mã")
    public ResponseEntity<QuotationResponse> getQuotationByNumber(
            @PathVariable String quotationNumber
    ) {
        log.info("REST request to get quotation by number: {}", quotationNumber);
        QuotationResponse response = quotationService.getQuotationByNumber(quotationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách báo giá")
    public ResponseEntity<Page<QuotationResponse>> getAllQuotations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "quotationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("REST request to get all quotations");
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QuotationResponse> response = quotationService.getAllQuotations(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Cập nhật báo giá", description = "Chỉ có thể cập nhật báo giá ở trạng thái DRAFT")
    public ResponseEntity<QuotationResponse> updateQuotation(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody QuotationRequest request
    ) {
        log.info("REST request to update quotation: {}", id);
        QuotationResponse response = quotationService.updateQuotation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa báo giá")
    public ResponseEntity<Void> deleteQuotation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to delete quotation: {}", id);
        quotationService.deleteQuotation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Gửi báo giá cho khách hàng", description = "Chuyển trạng thái sang SENT và gửi email")
    public ResponseEntity<QuotationResponse> sendQuotation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to send quotation: {}", id);
        QuotationResponse response = quotationService.sendQuotation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Chấp nhận báo giá", description = "Khách hàng chấp nhận báo giá")
    public ResponseEntity<QuotationResponse> acceptQuotation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to accept quotation: {}", id);
        QuotationResponse response = quotationService.acceptQuotation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Từ chối báo giá")
    public ResponseEntity<QuotationResponse> rejectQuotation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to reject quotation: {}", id);
        QuotationResponse response = quotationService.rejectQuotation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Chuyển báo giá thành đơn hàng",
            description = "Chuyển báo giá đã được chấp nhận thành đơn hàng chính thức")
    public ResponseEntity<QuotationResponse> convertToOrder(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to convert quotation to order: {}", id);
        QuotationResponse response = quotationService.convertToOrder(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/export-pdf")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Xuất báo giá ra PDF")
    public ResponseEntity<byte[]> exportQuotationToPdf(@PathVariable Long id) {
        log.info("REST request to export quotation to PDF: {}", id);
        byte[] pdfContent = quotationService.exportQuotationToPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "quotation-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN', 'CUSTOMER')")
    @Operation(summary = "Lấy báo giá của khách hàng")
    public ResponseEntity<List<QuotationResponse>> getQuotationsByCustomer(
            @PathVariable Long customerId
    ) {
        log.info("REST request to get quotations by customer: {}", customerId);
        List<QuotationResponse> response = quotationService.getQuotationsByCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales-person/{salesPersonId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy báo giá của nhân viên bán hàng")
    public ResponseEntity<List<QuotationResponse>> getQuotationsBySalesPerson(
            @PathVariable Long salesPersonId
    ) {
        log.info("REST request to get quotations by sales person: {}", salesPersonId);
        List<QuotationResponse> response = quotationService.getQuotationsBySalesPerson(salesPersonId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách báo giá đã hết hạn")
    public ResponseEntity<List<QuotationResponse>> getExpiredQuotations() {
        log.info("REST request to get expired quotations");
        List<QuotationResponse> response = quotationService.getExpiredQuotations();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/recalculate")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Tính lại giá báo giá", description = "Tính lại tổng giá sau khi thay đổi khuyến mãi")
    public ResponseEntity<QuotationResponse> recalculateQuotation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        log.info("REST request to recalculate quotation: {}", id);
        QuotationResponse response = quotationService.recalculateQuotation(id);
        return ResponseEntity.ok(response);
    }
}


package com.evm.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellInRequestResponse {
    private Long id;
    private String requestNumber; // Mã yêu cầu: SIR-2024-00001
    private LocalDate requestDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String status;

    // Dealer info
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;
    private String deliveryAddress;

    // Request details
    private List<SellInRequestItemResponse> items;
    private Integer totalQuantity;
    private BigDecimal totalAmount;

    private String notes;
    private String approvalNotes; // Ghi chú phê duyệt

    // Audit info
    private Long requestedBy; // User ID người tạo
    private String requestedByName;
    private Long approvedBy; // User ID người duyệt
    private String approvedByName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Status indicators
    private Boolean canApprove;
    private Boolean canReject;
    private Boolean canCancel;
    private Integer daysUntilExpectedDelivery;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellInRequestItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productVersion;
        private String productImageUrl;
        private Integer requestedQuantity;
        private Integer approvedQuantity;
        private Integer deliveredQuantity;
        private String color;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String notes;
    }
}
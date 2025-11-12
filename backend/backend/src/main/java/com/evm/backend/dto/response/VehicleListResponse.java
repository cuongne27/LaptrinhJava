package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for Vehicle list view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleListResponse {
    private String id;
    private String vin;
    private String color;
    private LocalDate manufactureDate;
    private String status;

    // Product info
    private Long productId;
    private String productName;
    private String productVersion;

    // Dealer info
    private Long dealerId;
    private String dealerName;

    // Statistics
    private Boolean hasSalesOrder; // Đã có đơn hàng chưa
    private Integer totalSupportTickets;
}
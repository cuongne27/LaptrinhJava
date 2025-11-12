package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for Vehicle detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDetailResponse {
    private String id;
    private String vin;
    private String batterySerial;
    private String color;
    private LocalDate manufactureDate;
    private String status;

    // Product info
    private Long productId;
    private String productName;
    private String productVersion;
    private String productImageUrl;

    // Dealer info
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;

    // Sales Order info (if sold)
    private Long salesOrderId;
    private String customerName;
    private LocalDate soldDate;

    // Statistics
    private Integer totalSupportTickets;
    private Integer openTickets;
    private Integer closedTickets;
}
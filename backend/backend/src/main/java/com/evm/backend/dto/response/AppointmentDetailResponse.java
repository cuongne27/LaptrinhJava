package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Appointment detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDetailResponse {
    private Long id;
    private OffsetDateTime appointmentTime;
    private String status;
    private String notes;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Staff info
    private Long staffUserId;
    private String staffName;
    private String staffEmail;

    // Product info
    private Long productId;
    private String productName;
    private String productVersion;
    private String productImageUrl;

    // Dealer info
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;
    private String dealerPhone;

    // Calculated fields
    private Boolean isUpcoming;
    private Boolean isToday;
    private Long hoursUntil;
    private Boolean canCancel; // Có thể cancel không (ví dụ: chỉ cancel nếu > 24h)
}
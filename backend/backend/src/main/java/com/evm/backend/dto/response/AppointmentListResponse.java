package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Appointment list view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentListResponse {
    private Long id;
    private OffsetDateTime appointmentTime;
    private String status;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Staff info
    private Long staffUserId;
    private String staffName;

    // Product info
    private Long productId;
    private String productName;

    // Dealer info
    private Long dealerId;
    private String dealerName;

    // Calculated fields
    private Boolean isUpcoming; // Chưa đến giờ hẹn
    private Boolean isToday; // Hẹn hôm nay
    private Long hoursUntil; // Số giờ còn lại (nếu upcoming)
}
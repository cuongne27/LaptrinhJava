package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Support Ticket Detail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketDetailResponse {
    private Long ticketId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private OffsetDateTime createdAt;
    private OffsetDateTime closedAt;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    // Assigned user info
    private Long assignedUserId;
    private String assignedUserName;
    private String assignedUserEmail;
    private String assignedUserRole;

    // Sales Order info
    private Long salesOrderId;
    private String orderReference;
    private String orderStatus;

    // Vehicle info
    private String vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleVin;

    // Statistics
    private Long daysOpen;
    private Long hoursToResolve; // Thời gian xử lý (nếu đã closed)
}
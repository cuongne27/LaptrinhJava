package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Support Ticket List
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketListResponse {
    private Long id;
    private String title;
    private String status;
    private String priority;
    private String category;
    private OffsetDateTime createdAt;
    private OffsetDateTime closedAt;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Assigned user info
    private Long assignedUserId;
    private String assignedUserName;

    // Related info
    private Long salesOrderId;
    private String vehicleId;

    // Duration
    private Long daysOpen; // Số ngày mở ticket
}
package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Request DTO for filtering support tickets
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketFilterRequest {

    private String searchKeyword; // Search in title, description
    private String status; // OPEN, PENDING, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String category; // WARRANTY, TECHNICAL, SALES, DELIVERY, MAINTENANCE, OTHER
    private Long customerId;
    private Long assignedUserId;
    private Long salesOrderId;
    private String vehicleId;
    private OffsetDateTime fromDate; // Filter from createdAt
    private OffsetDateTime toDate; // Filter to createdAt

    // Sorting: created_asc, created_desc, title_asc, title_desc, priority_desc
    private String sortBy;

    private Integer page;
    private Integer size;
}
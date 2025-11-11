package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for Customer detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String address;
    private String customerType;
    private String history;
    private OffsetDateTime createdAt;

    // Statistics
    private Long totalOrders;
    private Long totalSupportTickets;
    private Long openTickets;
    private Long closedTickets;
}
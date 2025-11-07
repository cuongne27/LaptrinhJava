package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Dealer list view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerListResponse {
    private Long id;
    private String dealerName;
    private String address;
    private String phoneNumber;
    private String email;
    private String dealerLevel;

    // Brand info
    private Integer brandId;
    private String brandName;

    // Statistics
    private Long totalUsers;
    private Long totalVehicles;
    private Long totalAppointments;
}
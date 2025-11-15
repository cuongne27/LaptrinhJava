package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for User Detail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Boolean isActive;
    private OffsetDateTime dateJoined;

    // Role info
    private Long roleId;
    private String roleName;
    private String roleDisplayName;
    private String roleType;
    private String roleDescription;

    // Brand info
    private Integer brandId;
    private String brandName;
    private String brandContactInfo;

    // Dealer info
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;
    private String dealerPhone;
    private String dealerEmail;
    private String dealerLevel;

    // Statistics
    private Integer totalAppointments;
    private Integer totalDistributionOrders;
}
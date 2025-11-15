package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for User List
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Boolean isActive;

    // Role info
    private Long roleId;
    private String roleName;
    private String roleDisplayName;

    // Brand info
    private Integer brandId;
    private String brandName;

    // Dealer info
    private Long dealerId;
    private String dealerName;

    private OffsetDateTime dateJoined;
}
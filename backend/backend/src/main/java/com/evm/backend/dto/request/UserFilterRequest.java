package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering users
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFilterRequest {

    private String searchKeyword; // Search in username, fullName, email
    private String roleName; // ADMIN, BRAND_MANAGER, DEALER_STAFF, etc.
    private Long roleId;
    private Integer brandId;
    private Long dealerId;
    private Boolean isActive; // true/false/null (all)

    // Sorting: username_asc, username_desc, fullname_asc, fullname_desc, created_asc, created_desc
    private String sortBy;

    private Integer page;
    private Integer size;
}
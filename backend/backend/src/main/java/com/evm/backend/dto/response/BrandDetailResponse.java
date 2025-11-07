package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Brand detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandDetailResponse {
    private Integer id;
    private String brandName;
    private String headquartersAddress;
    private String taxCode;
    private String contactInfo;

    // Statistics
    private Long totalDealers;
    private Long totalProducts;
    private Long totalUsers;
    private Long totalContracts;
}
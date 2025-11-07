package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering dealers
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerFilterRequest {
    private Integer brandId;
    private String searchKeyword; // Search in name, address, email
    private String dealerLevel;
    private String sortBy; // name_asc, name_desc, level_asc, level_desc
    private Integer page;
    private Integer size;
}
package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for filtering dealer contracts
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerContractFilterRequest {
    private Integer brandId;
    private Long dealerId;
    private LocalDate startDate; // Filter contracts starting from this date
    private LocalDate endDate; // Filter contracts ending before this date
    private String status; // ACTIVE, EXPIRED, UPCOMING
    private String sortBy; // start_date_asc, start_date_desc, end_date_asc, end_date_desc
    private Integer page;
    private Integer size;
}
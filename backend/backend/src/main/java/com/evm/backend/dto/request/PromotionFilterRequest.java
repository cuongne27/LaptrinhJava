package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for filtering promotions
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionFilterRequest {
    private String searchKeyword; // Search in code, name, description
    private String discountType;
    private String status; // ACTIVE, UPCOMING, EXPIRED
    private LocalDate fromDate;
    private LocalDate toDate;
    private String sortBy; // code_asc, code_desc, start_date_asc, start_date_desc
    private Integer page;
    private Integer size;
}
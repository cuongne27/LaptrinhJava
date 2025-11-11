package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering customers
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerFilterRequest {
    private String searchKeyword; // Search in name, phone, email, address
    private String customerType;
    private String sortBy; // name_asc, name_desc, created_asc, created_desc
    private Integer page;
    private Integer size;
}
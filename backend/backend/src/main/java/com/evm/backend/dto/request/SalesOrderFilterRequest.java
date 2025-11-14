package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Request DTO for filtering sales orders
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderFilterRequest {
    private Long customerId;
    private Long salesPersonId;
    private String vehicleId;
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String sortBy; // date_asc, date_desc, price_asc, price_desc, status_asc, status_desc
    private Integer page;
    private Integer size;
}
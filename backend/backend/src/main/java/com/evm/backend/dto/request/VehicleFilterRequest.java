package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for filtering vehicles
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleFilterRequest {
    private Long productId;
    private Long dealerId;
    private String color;
    private String status;
    private LocalDate manufactureFromDate;
    private LocalDate manufactureToDate;
    private String searchKeyword; // Search in id, vin, battery serial
    private String sortBy; // date_asc, date_desc, status_asc, status_desc
    private Integer page;
    private Integer size;
}
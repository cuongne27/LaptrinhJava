package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Request DTO for filtering appointments
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentFilterRequest {
    private Long customerId;
    private Long staffUserId;
    private Long productId;
    private Long dealerId;
    private String status;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private String sortBy; // time_asc, time_desc, status_asc, status_desc
    private Integer page;
    private Integer size;
}
package com.evm.backend.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellInRequestFilterRequest {
    private Long dealerId;
    private Long brandId;
    private String status; // PENDING, APPROVED, REJECTED, IN_TRANSIT, DELIVERED
    private java.time.LocalDate fromDate;
    private java.time.LocalDate toDate;
    private String sortBy;
    private Integer page;
    private Integer size;
}
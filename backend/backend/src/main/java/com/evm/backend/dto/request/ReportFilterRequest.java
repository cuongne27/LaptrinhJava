package com.evm.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filter request cho các báo cáo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Long dealerId;
    private Long salesPersonId;
    private Long customerId;
    private Long productId;
    private String status;

    // Grouping options: DAY, WEEK, MONTH, QUARTER, YEAR
    private String groupBy;

    // Export format: PDF, EXCEL, CSV
    private String exportFormat;
}
package com.evm.backend.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ForecastFilterRequest {
    private Long productId;
    private Long brandId;
    private String forecastPeriod;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "forecast_date_desc"; // forecast_date_asc, confidence_desc, etc.
}
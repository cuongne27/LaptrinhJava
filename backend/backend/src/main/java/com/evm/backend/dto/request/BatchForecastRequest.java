package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BatchForecastRequest {

    @NotEmpty(message = "Danh sách product IDs không được để trống")
    private List<Long> productIds;

    @NotBlank(message = "Forecast period không được để trống")
    private String forecastPeriod;

    @NotNull(message = "Forecast date không được để trống")
    private LocalDate forecastDate;

    @Min(1)
    @Max(12)
    private Integer numberOfPeriods = 3;

    private String forecastMethod = "LINEAR_REGRESSION";
}

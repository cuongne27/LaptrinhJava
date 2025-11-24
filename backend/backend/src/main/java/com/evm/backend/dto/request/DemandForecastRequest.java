package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemandForecastRequest {

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotBlank(message = "Forecast period không được để trống")
    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY", message = "Period phải là MONTHLY, QUARTERLY, hoặc YEARLY")
    private String forecastPeriod;

    @NotNull(message = "Forecast date không được để trống")
    private LocalDate forecastDate;

    @Min(value = 1, message = "Số tháng dự báo phải >= 1")
    @Max(value = 24, message = "Số tháng dự báo phải <= 24")
    private Integer numberOfPeriods; // Số kỳ cần dự báo

    @Pattern(regexp = "LINEAR_REGRESSION|MOVING_AVERAGE|EXPONENTIAL_SMOOTHING",
            message = "Method không hợp lệ")
    private String forecastMethod;

    private String notes;
}

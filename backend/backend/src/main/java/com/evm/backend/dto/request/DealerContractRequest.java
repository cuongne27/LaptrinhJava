package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unified Request DTO for DealerContract (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerContractRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private String contractTerms; // Điều khoản hợp đồng

    @NotNull(message = "Tỷ lệ hoa hồng không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ hoa hồng phải >= 0")
    @DecimalMax(value = "100.0", message = "Tỷ lệ hoa hồng phải <= 100")
    private BigDecimal commissionRate; // Tỷ lệ % (VD: 5.5 = 5.5%)

    @NotNull(message = "Mục tiêu doanh số không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Mục tiêu doanh số phải > 0")
    private BigDecimal salesTarget;

    @NotNull(message = "Brand ID không được để trống")
    private Integer brandId;

    @NotNull(message = "Dealer ID không được để trống")
    private Long dealerId;
}
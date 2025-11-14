package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Unified Request DTO for SalesOrder (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderRequest {

    @NotNull(message = "Ngày đặt hàng không được để trống")
    private LocalDate orderDate;

    @NotNull(message = "Giá cơ bản không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
    private BigDecimal basePrice;

    @NotNull(message = "VAT không được để trống")
    @DecimalMin(value = "0.0", message = "VAT phải lớn hơn hoặc bằng 0")
    private BigDecimal vat;

    @DecimalMin(value = "0.0", message = "Phí đăng ký phải lớn hơn hoặc bằng 0")
    private BigDecimal registrationFee;

    @DecimalMin(value = "0.0", message = "Giảm giá phải lớn hơn hoặc bằng 0")
    private BigDecimal discountAmount;

    @NotNull(message = "Tổng giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng giá phải lớn hơn 0")
    private BigDecimal totalPrice;

    private String status; // PENDING, CONFIRMED, PAID, COMPLETED, CANCELLED

    @NotNull(message = "Vehicle ID không được để trống")
    private String vehicleId;

    @NotNull(message = "Customer ID không được để trống")
    private Long customerId;

    @NotNull(message = "Sales Person ID không được để trống")
    private Long salesPersonId;

    private Set<Long> promotionIds;
}
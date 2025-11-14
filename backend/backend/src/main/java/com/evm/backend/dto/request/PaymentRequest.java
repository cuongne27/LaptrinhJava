package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for Payment (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID không được để trống")
    private Long orderId;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Ngày thanh toán không được để trống")
    private LocalDate paymentDate;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Pattern(
            regexp = "CASH|BANK_TRANSFER|CREDIT_CARD|INSTALLMENT|COMPANY_TRANSFER|QR_CODE",
            message = "Phương thức thanh toán không hợp lệ"
    )
    private String paymentMethod;

    @Pattern(
            regexp = "PENDING|COMPLETED|FAILED|REFUNDED",
            message = "Trạng thái thanh toán không hợp lệ"
    )
    private String status; // Default: PENDING

    private String referenceNumber; // Mã tham chiếu giao dịch

    @Pattern(
            regexp = "ORDER_PAYMENT|DEPOSIT|INSTALLMENT|FINAL_PAYMENT",
            message = "Loại thanh toán không hợp lệ"
    )
    private String paymentType; // Loại thanh toán

    private Long payerId; // ID Customer thanh toán (nếu khác với customer của order)
}
package com.evm.backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Unified Request DTO for Appointment (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Thời gian hẹn không được để trống")
    @Future(message = "Thời gian hẹn phải là thời điểm trong tương lai")
    private OffsetDateTime appointmentTime;

    private String status; // SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW

    private String notes;

    @NotNull(message = "Customer ID không được để trống")
    private Long customerId;

    private Long staffUserId; // Optional - có thể auto assign

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Dealer ID không được để trống")
    private Long dealerId;
}
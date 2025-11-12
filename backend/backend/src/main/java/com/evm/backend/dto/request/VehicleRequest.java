package com.evm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Unified Request DTO for Vehicle (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Vehicle ID không được để trống")
    @Size(max = 50, message = "Vehicle ID không được vượt quá 50 ký tự")
    private String id; // Vehicle ID (chassis number hoặc unique ID)

    @NotBlank(message = "VIN không được để trống")
    @Size(max = 50, message = "VIN không được vượt quá 50 ký tự")
    private String vin; // Vehicle Identification Number

    @Size(max = 50, message = "Battery serial không được vượt quá 50 ký tự")
    private String batterySerial;

    @Size(max = 30, message = "Màu xe không được vượt quá 30 ký tự")
    private String color;

    private LocalDate manufactureDate;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    private String status; // AVAILABLE, SOLD, RESERVED, IN_TRANSIT, DAMAGED

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Dealer ID không được để trống")
    private Long dealerId;
}
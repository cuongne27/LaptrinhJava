package com.evm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignVehicleRequest {

    @NotBlank(message = "Vehicle ID (VIN) không được để trống")
    private String vehicleId; // VIN number

    private String notes; // Ghi chú khi gán xe
}
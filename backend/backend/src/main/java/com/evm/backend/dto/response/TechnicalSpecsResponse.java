package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thông số kỹ thuật
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalSpecsResponse {
    private String batteryCapacity;
    private String productRange;
    private String power;
    private String maxSpeed;
    private String chargingTime;
    private String dimensions;
    private String weight;
    private String seatingCapacity;
}

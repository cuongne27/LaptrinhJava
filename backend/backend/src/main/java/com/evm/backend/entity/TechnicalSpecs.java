package com.evm.backend.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalSpecs {
    private String batteryCapacity;
    private String productRange;
    private String power;
    private String maxSpeed;
    private String chargingTime;

    @Column(name = "specs_dimensions")
    private String dimensions;

    @Column(name = "specs_weight")
    private String weight;

    private String seatingCapacity;
}

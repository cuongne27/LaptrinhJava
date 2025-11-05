package com.evm.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class TechnicalSpecsRequest {

    @Size(max = 100, message = "Dung lượng pin không được vượt quá 100 ký tự")
    private String batteryCapacity; // VD: "82 kWh"

    @Size(max = 100, message = "Quãng đường không được vượt quá 100 ký tự")
    private String productRange; // VD: "420 km"

    @Size(max = 100, message = "Công suất không được vượt quá 100 ký tự")
    private String power; // VD: "300 kW"

    @Size(max = 100, message = "Tốc độ tối đa không được vượt quá 100 ký tự")
    private String maxSpeed; // VD: "200 km/h"

    @Size(max = 100, message = "Thời gian sạc không được vượt quá 100 ký tự")
    private String chargingTime; // VD: "30 phút"

    @Size(max = 255, message = "Kích thước không được vượt quá 255 ký tự")
    private String dimensions; // VD: "4750x1934x1667 mm"

    @Size(max = 100, message = "Trọng lượng không được vượt quá 100 ký tự")
    private String weight; // VD: "2100 kg"

    @Size(max = 100, message = "Số chỗ ngồi không được vượt quá 100 ký tự")
    private String seatingCapacity; // VD: "5 chỗ"
}

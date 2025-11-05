package com.evm.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantRequest {

    @NotBlank(message = "Tên màu không được để trống")
    @Size(max = 50, message = "Tên màu không được vượt quá 50 ký tự")
    private String color;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Mã màu phải là hex color (VD: #FFFFFF)")
    private String colorCode;

    @Min(value = 0, message = "Số lượng phải >= 0")
    private Long availableQuantity;
}

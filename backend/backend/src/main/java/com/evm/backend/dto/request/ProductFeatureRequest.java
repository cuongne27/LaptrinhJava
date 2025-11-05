package com.evm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFeatureRequest {

    @NotBlank(message = "Tên tính năng không được để trống")
    @Size(max = 100, message = "Tên tính năng không được vượt quá 100 ký tự")
    private String featureName;

    private String description;

    @Size(max = 255, message = "URL icon không được vượt quá 255 ký tự")
    private String iconUrl;
}

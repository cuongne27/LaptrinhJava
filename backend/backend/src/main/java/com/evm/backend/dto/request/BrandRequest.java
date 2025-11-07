package com.evm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified Request DTO for Brand (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 100, message = "Tên thương hiệu không được vượt quá 100 ký tự")
    private String brandName;

    @Size(max = 255, message = "Địa chỉ trụ sở không được vượt quá 255 ký tự")
    private String headquartersAddress;

    @Size(max = 20, message = "Mã số thuế không được vượt quá 20 ký tự")
    private String taxCode;

    @Size(max = 255, message = "Thông tin liên hệ không được vượt quá 255 ký tự")
    private String contactInfo;
}
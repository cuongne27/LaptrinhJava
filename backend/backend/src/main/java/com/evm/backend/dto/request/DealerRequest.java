package com.evm.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified Request DTO for Dealer (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerRequest {

    @NotBlank(message = "Tên đại lý không được để trống")
    @Size(max = 150, message = "Tên đại lý không được vượt quá 150 ký tự")
    private String dealerName;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 50, message = "Cấp độ đại lý không được vượt quá 50 ký tự")
    private String dealerLevel; // VD: "Gold", "Silver", "Bronze", "Platinum"

    @NotNull(message = "Brand ID không được để trống")
    private Integer brandId;
}
package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for User (CREATE & UPDATE)
 * Admin tạo tài khoản cho nhân viên
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username chỉ chứa chữ, số, dấu chấm, gạch dưới, gạch ngang")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password; // Chỉ dùng khi CREATE, không dùng khi UPDATE

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotNull(message = "Role ID không được để trống")
    private Long roleId;

    private Integer brandId; // Nullable - chỉ với BRAND_MANAGER

    private Long dealerId; // Nullable - chỉ với DEALER_STAFF, DEALER_MANAGER

    @Builder.Default
    private Boolean isActive = true; // Default active khi tạo mới
}
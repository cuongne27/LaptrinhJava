package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Inventory (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    private Long dealerId; // NULL = Brand warehouse

    @NotNull(message = "Tổng số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer totalQuantity;

    @NotNull(message = "Số lượng reserved không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer reservedQuantity;

    @NotNull(message = "Số lượng available không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer availableQuantity;

    @NotNull(message = "Số lượng in-transit không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer inTransitQuantity;

    @Size(max = 100, message = "Location không được vượt quá 100 ký tự")
    private String location; // Vị trí kho (Khu A, Kệ B1, etc.)
}
package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellInRequestRequest {

    @NotNull(message = "Dealer ID không được để trống")
    private Long dealerId;

    @NotNull(message = "Ngày yêu cầu không được để trống")
    private LocalDate requestDate;

    private LocalDate expectedDeliveryDate; // Ngày dự kiến giao

    @NotEmpty(message = "Phải có ít nhất 1 sản phẩm")
    private List<SellInRequestDetailDto> items;

    private String notes;
    private String deliveryAddress;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellInRequestDetailDto {
        @NotNull(message = "Product ID không được để trống")
        private Long productId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải >= 1")
        private Integer quantity;

        private String color; // Màu sắc yêu cầu

        private String notes;
    }
}
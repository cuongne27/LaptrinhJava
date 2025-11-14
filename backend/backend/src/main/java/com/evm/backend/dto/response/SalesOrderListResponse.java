package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for SalesOrder list view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderListResponse {
    private Long id;
    private LocalDate orderDate;
    private BigDecimal totalPrice;
    private String status;

    // Vehicle info
    private String vehicleId;
    private String vehicleModel;
    private String vehicleBrand;
    private String vehicleVin;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Sales person info
    private Long salesPersonId;
    private String salesPersonName;

    // Calculated fields
    private Integer daysFromOrder; // Số ngày từ khi đặt hàng
    private Boolean isPaid; // Đã thanh toán đủ chưa
    private BigDecimal paidAmount; // Số tiền đã thanh toán
    private BigDecimal remainingAmount; // Số tiền còn lại
}
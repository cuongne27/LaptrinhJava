package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderResponse {
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

    // Quotation reference (nếu order được tạo từ quotation)
    private Long quotationId;
    private String quotationNumber;

    // Calculated fields
    private Integer daysFromOrder;
    private Boolean isPaid;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    // Message for client
    private String message;
}
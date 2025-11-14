package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Promotion list view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionListResponse {
    private Long id;
    private String promotionCode;
    private String promotionName;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, UPCOMING, EXPIRED

    // Statistics
    private Long totalUsages; // Số lần đã sử dụng
    private Long daysRemaining; // Số ngày còn lại (nếu active)
}
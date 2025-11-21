package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * D.2: Inventory Report Response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReportResponse {
    private LocalDate reportDate;

    // Tổng hợp
    private Integer totalProducts;
    private Integer totalStock;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer inTransitStock;

    // Cảnh báo
    private Integer lowStockCount;
    private Integer outOfStockCount;

    // Chi tiết tồn kho
    private List<InventoryDetail> inventoryDetails;

    // Cảnh báo cụ thể
    private List<StockAlert> alerts;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InventoryDetail {
        private Long productId;
        private String productName;
        private String version;
        private Long dealerId;
        private String dealerName;
        private String location;
        private Integer totalQuantity;
        private Integer availableQuantity;
        private Integer reservedQuantity;
        private Integer inTransitQuantity;
        private String stockStatus; // NORMAL, LOW_STOCK, OUT_OF_STOCK
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockAlert {
        private Long productId;
        private String productName;
        private Long dealerId;
        private String dealerName;
        private String alertType; // LOW_STOCK, OUT_OF_STOCK, OVERSTOCK
        private Integer currentQuantity;
        private Integer minStockLevel;
        private String message;
    }
}
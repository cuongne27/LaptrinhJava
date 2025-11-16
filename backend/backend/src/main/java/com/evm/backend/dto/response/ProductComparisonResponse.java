package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductComparisonResponse {
    private List<ProductDetail> products;
    private ComparisonSummary summary;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDetail {
        private Long id;
        private String productName;
        private String version;
        private String brandName;
        private String imageUrl;
        private String videoUrl;

        // Technical specs từ TechnicalSpecs embedded
        private Integer range;              // Quãng đường (km)
        private Integer power;              // Công suất (HP)
        private BigDecimal batteryCapacity; // Dung lượng pin (kWh)
        private Integer topSpeed;           // Tốc độ tối đa
        private BigDecimal acceleration;    // 0-100 km/h (giây)
        private Integer chargingTime;       // Thời gian sạc (phút)
        private String motorType;           // Loại động cơ
        private Integer weight;             // Trọng lượng (kg)

        // Price
        private BigDecimal msrp;            // Giá niêm yết

        // Features
        private List<String> features;      // Danh sách tính năng

        // Variants available
        private List<VariantInfo> variants;

        // Comparison highlights
        private List<String> advantages;    // Ưu điểm nổi bật
        private List<String> disadvantages; // Nhược điểm
        private String recommendation;      // Khuyến nghị cho ai
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantInfo {
        private Long variantId;
        private String variantName;
        private BigDecimal price;
        private String color;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComparisonSummary {
        private ProductBest bestRange;
        private ProductBest bestPower;
        private ProductBest bestBattery;
        private ProductBest fastestCharging;
        private ProductBest cheapest;
        private ProductBest mostExpensive;
        private ProductBest bestValue;      // Giá trị tốt nhất (giá/hiệu suất)

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ProductBest {
            private Long productId;
            private String productName;
            private Object value;
        }
    }
}
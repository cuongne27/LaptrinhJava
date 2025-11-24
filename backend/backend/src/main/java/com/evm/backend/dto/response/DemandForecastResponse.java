package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemandForecastResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productVersion;
    private String brandName;
    private String forecastPeriod;
    private LocalDate forecastDate;
    private Integer predictedDemand;
    private BigDecimal confidenceScore;
    private Integer actualDemand;
    private Integer accuracy; // Calculated: (1 - |actual - predicted| / actual) * 100
    private String forecastMethod;
    private Integer historicalDataPoints;
    private BigDecimal seasonalityFactor;
    private BigDecimal trendFactor;
    private BigDecimal marketGrowthRate;
    private String status;
    private String notes;
    private String createdByName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Metadata cho AI insights
    private ForecastInsights insights;

    @Data
    @Builder
    public static class ForecastInsights {
        private String trend; // INCREASING, DECREASING, STABLE
        private BigDecimal trendPercentage;
        private String seasonalPattern; // HIGH_SEASON, LOW_SEASON, NORMAL
        private List<String> influencingFactors;
        private String recommendation;
    }
}
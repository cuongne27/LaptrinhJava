package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "demand_forecasts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "forecast_period", nullable = false, length = 20)
    private String forecastPeriod; // MONTHLY, QUARTERLY, YEARLY

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate; // Ngày bắt đầu của kỳ dự báo

    @Column(name = "predicted_demand", nullable = false)
    private Integer predictedDemand;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore; // 0-100%

    @Column(name = "actual_demand")
    private Integer actualDemand; // Cập nhật sau khi có số liệu thực tế

    @Column(name = "forecast_method", length = 50)
    private String forecastMethod; // LINEAR_REGRESSION, ARIMA, PROPHET, etc.

    @Column(name = "historical_data_points")
    private Integer historicalDataPoints; // Số data points được sử dụng

    @Column(name = "seasonality_factor", precision = 5, scale = 2)
    private BigDecimal seasonalityFactor;

    @Column(name = "trend_factor", precision = 5, scale = 2)
    private BigDecimal trendFactor;

    @Column(name = "market_growth_rate", precision = 5, scale = 2)
    private BigDecimal marketGrowthRate;

    @Column(name = "status", length = 20)
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
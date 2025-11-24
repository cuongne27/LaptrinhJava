package com.evm.backend.service;

import com.evm.backend.dto.request.BatchForecastRequest;
import com.evm.backend.dto.request.DemandForecastRequest;
import com.evm.backend.dto.request.ForecastFilterRequest;
import com.evm.backend.dto.response.DemandForecastResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DemandForecastService {

    /**
     * Create a demand forecast for a product
     */
    DemandForecastResponse createForecast(DemandForecastRequest request, Long userId);

    /**
     * Create forecasts for multiple products at once
     */
    List<DemandForecastResponse> createBatchForecast(BatchForecastRequest request, Long userId);

    /**
     * Get forecast by ID
     */
    DemandForecastResponse getForecastById(Long id);

    /**
     * Get all forecasts with filters
     */
    Page<DemandForecastResponse> getForecasts(ForecastFilterRequest filter);

    /**
     * Update actual demand after the forecast period ends
     */
    DemandForecastResponse updateActualDemand(Long forecastId, Integer actualDemand);

    /**
     * Get forecast accuracy metrics
     */
    Map<String, Object> getForecastAccuracy(Long productId, LocalDate fromDate);

    /**
     * Delete a forecast
     */
    void deleteForecast(Long id);
}
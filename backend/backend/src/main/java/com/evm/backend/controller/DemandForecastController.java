package com.evm.backend.controller;

import com.evm.backend.dto.request.BatchForecastRequest;
import com.evm.backend.dto.request.DemandForecastRequest;
import com.evm.backend.dto.request.ForecastFilterRequest;
import com.evm.backend.dto.response.DemandForecastResponse;
import com.evm.backend.service.DemandForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demand-forecasts")
@RequiredArgsConstructor
@Slf4j
public class DemandForecastController {

    private final DemandForecastService forecastService;

    /**
     * Create a single demand forecast
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<DemandForecastResponse> createForecast(
            @Valid @RequestBody DemandForecastRequest request,
            @RequestParam Long userId
    ) {
        log.info("REST request to create demand forecast for product: {}", request.getProductId());
        DemandForecastResponse response = forecastService.createForecast(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create batch forecasts for multiple products
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<List<DemandForecastResponse>> createBatchForecast(
            @Valid @RequestBody BatchForecastRequest request,
            @RequestParam Long userId
    ) {
        log.info("REST request to create batch forecasts for {} products", request.getProductIds().size());
        List<DemandForecastResponse> responses = forecastService.createBatchForecast(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Get forecast by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DemandForecastResponse> getForecastById(@PathVariable Long id) {
        log.info("REST request to get demand forecast: {}", id);
        DemandForecastResponse response = forecastService.getForecastById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all forecasts with filters
     */
    @GetMapping
    public ResponseEntity<Page<DemandForecastResponse>> getForecasts(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String forecastPeriod,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "forecast_date_desc") String sortBy
    ) {
        log.info("REST request to get demand forecasts with filters");

        ForecastFilterRequest filter = new ForecastFilterRequest();
        filter.setProductId(productId);
        filter.setBrandId(brandId);
        filter.setForecastPeriod(forecastPeriod);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setStatus(status);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);

        Page<DemandForecastResponse> forecasts = forecastService.getForecasts(filter);
        return ResponseEntity.ok(forecasts);
    }

    /**
     * Update actual demand for a forecast
     */
    @PatchMapping("/{id}/actual-demand")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<DemandForecastResponse> updateActualDemand(
            @PathVariable Long id,
            @RequestParam Integer actualDemand
    ) {
        log.info("REST request to update actual demand for forecast: {}", id);
        DemandForecastResponse response = forecastService.updateActualDemand(id, actualDemand);
        return ResponseEntity.ok(response);
    }

    /**
     * Get forecast accuracy metrics
     */
    @GetMapping("/accuracy")
    public ResponseEntity<Map<String, Object>> getForecastAccuracy(
            @RequestParam Long productId,
            @RequestParam(required = false) LocalDate fromDate
    ) {
        log.info("REST request to get forecast accuracy for product: {}", productId);

        if (fromDate == null) {
            fromDate = LocalDate.now().minusMonths(6);
        }

        Map<String, Object> accuracy = forecastService.getForecastAccuracy(productId, fromDate);
        return ResponseEntity.ok(accuracy);
    }

    /**
     * Delete a forecast
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteForecast(@PathVariable Long id) {
        log.info("REST request to delete demand forecast: {}", id);
        forecastService.deleteForecast(id);
        return ResponseEntity.noContent().build();
    }
}
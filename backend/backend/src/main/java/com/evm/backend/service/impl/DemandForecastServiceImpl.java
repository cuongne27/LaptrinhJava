//package com.evm.backend.service.impl;
//
//import com.evm.backend.dto.request.BatchForecastRequest;
//import com.evm.backend.dto.request.DemandForecastRequest;
//import com.evm.backend.dto.request.ForecastFilterRequest;
//import com.evm.backend.dto.response.DemandForecastResponse;
//import com.evm.backend.entity.*;
//import com.evm.backend.exception.BadRequestException;
//import com.evm.backend.exception.ResourceNotFoundException;
//import com.evm.backend.repository.*;
//import com.evm.backend.service.DemandForecastService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional(readOnly = true)
//public class DemandForecastServiceImpl implements DemandForecastService {
//
//    private final DemandForecastRepository forecastRepository;
//    private final ProductRepository productRepository;
//    private final SalesOrderRepository salesOrderRepository;
//    private final InventoryRepository inventoryRepository;
//    private final UserRepository userRepository;
//
//    @Override
//    @Transactional
//    public DemandForecastResponse createForecast(DemandForecastRequest request, Long userId) {
//        log.info("Creating demand forecast for product: {}", request.getProductId());
//
//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        // Get historical sales data
//        List<HistoricalSalesData> historicalData = getHistoricalSalesData(
//                request.getProductId(),
//                request.getForecastPeriod()
//        );
//
//        if (historicalData.size() < 3) {
//            throw new BadRequestException("Không đủ dữ liệu lịch sử (cần tối thiểu 3 tháng)");
//        }
//
//        // Calculate forecast using selected method
//        ForecastResult result = calculateForecast(
//                historicalData,
//                request.getForecastMethod() != null ? request.getForecastMethod() : "LINEAR_REGRESSION"
//        );
//
//        // Create forecast entity
//        DemandForecast forecast = DemandForecast.builder()
//                .product(product)
//                .forecastPeriod(request.getForecastPeriod())
//                .forecastDate(request.getForecastDate())
//                .predictedDemand(result.getPredictedDemand())
//                .confidenceScore(result.getConfidenceScore())
//                .forecastMethod(result.getMethod())
//                .historicalDataPoints(historicalData.size())
//                .seasonalityFactor(result.getSeasonalityFactor())
//                .trendFactor(result.getTrendFactor())
//                .marketGrowthRate(result.getMarketGrowthRate())
//                .status("PUBLISHED")
//                .notes(request.getNotes())
//                .createdBy(user)
//                .build();
//
//        DemandForecast saved = forecastRepository.save(forecast);
//        log.info("Forecast created successfully: {}", saved.getId());
//
//        return convertToResponse(saved);
//    }
//
//    @Override
//    @Transactional
//    public List<DemandForecastResponse> createBatchForecast(BatchForecastRequest request, Long userId) {
//        log.info("Creating batch forecasts for {} products", request.getProductIds().size());
//
//        List<DemandForecastResponse> results = new ArrayList<>();
//
//        for (Long productId : request.getProductIds()) {
//            try {
//                DemandForecastRequest singleRequest = DemandForecastRequest.builder()
//                        .productId(productId)
//                        .forecastPeriod(request.getForecastPeriod())
//                        .forecastDate(request.getForecastDate())
//                        .numberOfPeriods(request.getNumberOfPeriods())
//                        .forecastMethod(request.getForecastMethod())
//                        .build();
//
//                DemandForecastResponse forecast = createForecast(singleRequest, userId);
//                results.add(forecast);
//            } catch (Exception e) {
//                log.error("Error creating forecast for product {}: {}", productId, e.getMessage());
//            }
//        }
//
//        return results;
//    }
//
//    @Override
//    public DemandForecastResponse getForecastById(Long id) {
//        DemandForecast forecast = forecastRepository.findByIdWithDetails(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Forecast not found"));
//        return convertToResponse(forecast);
//    }
//
//    @Override
//    public Page<DemandForecastResponse> getForecasts(ForecastFilterRequest filter) {
//        Pageable pageable = buildPageable(filter);
//
//        Page<DemandForecast> forecasts = forecastRepository.findWithFilters(
//                filter.getProductId(),
//                filter.getBrandId(),
//                filter.getForecastPeriod(),
//                filter.getFromDate(),
//                filter.getToDate(),
//                filter.getStatus(),
//                pageable
//        );
//
//        return forecasts.map(this::convertToResponse);
//    }
//
//    @Override
//    @Transactional
//    public DemandForecastResponse updateActualDemand(Long forecastId, Integer actualDemand) {
//        DemandForecast forecast = forecastRepository.findById(forecastId)
//                .orElseThrow(() -> new ResourceNotFoundException("Forecast not found"));
//
//        forecast.setActualDemand(actualDemand);
//        DemandForecast updated = forecastRepository.save(forecast);
//
//        log.info("Updated actual demand for forecast {}: {}", forecastId, actualDemand);
//        return convertToResponse(updated);
//    }
//
//    @Override
//    public Map<String, Object> getForecastAccuracy(Long productId, LocalDate fromDate) {
//        List<DemandForecast> forecasts = forecastRepository.findHistoricalForecastsWithActuals(
//                productId,
//                PageRequest.of(0, 100)
//        );
//
//        if (forecasts.isEmpty()) {
//            return Map.of("message", "No historical data available");
//        }
//
//        double avgAccuracy = forecastRepository.calculateAverageAccuracy(productId, fromDate);
//
//        List<Map<String, ? extends Comparable<? extends Comparable<?>>>> details = forecasts.stream()
//                .map(f -> {
//                    int accuracy = calculateAccuracy(f.getPredictedDemand(), f.getActualDemand());
//                    return Map.of(
//                            "date", f.getForecastDate(),
//                            "predicted", f.getPredictedDemand(),
//                            "actual", f.getActualDemand(),
//                            "accuracy", accuracy
//                    );
//                })
//                .collect(Collectors.toList());
//
//        return Map.of(
//                "averageAccuracy", avgAccuracy,
//                "totalForecasts", forecasts.size(),
//                "details", details
//        );
//    }
//
//    @Override
//    public void deleteForecast(Long id) {
//
//    }
//
//    // ==================== PRIVATE HELPER METHODS ====================
//
////    private List<HistoricalSalesData> getHistoricalSalesData(Long productId, String period) {
////        LocalDate endDate = LocalDate.now();
////        LocalDate startDate = endDate.minusMonths(12); // Get last 12 months
////
////        List<HistoricalSalesData> data = new ArrayList<>();
////
////        LocalDate currentDate = startDate;
////        while (!currentDate.isAfter(endDate)) {
////            LocalDate periodStart = currentDate.withDayOfMonth(1);
////            LocalDate periodEnd = currentDate.plusMonths(1).withDayOfMonth(1).minusDays(1);
////
////            // Query actual sales orders for this product in this period
////            Integer quantity = salesOrderRepository.countByProductAndDateRange(
////                    productId,
////                    periodStart.atStartOfDay().atOffset(java.time.ZoneOffset.UTC),
////                    periodEnd.atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC)
////            );
////
////            if (quantity == null) quantity = 0;
////
////            data.add(new HistoricalSalesData(periodStart, quantity));
////            currentDate = currentDate.plusMonths(1);
////        }
////
////        return data;
////    }
//
//    private ForecastResult calculateForecast(List<HistoricalSalesData> historicalData, String method) {
//        switch (method) {
//            case "MOVING_AVERAGE":
//                return calculateMovingAverage(historicalData);
//            case "EXPONENTIAL_SMOOTHING":
//                return calculateExponentialSmoothing(historicalData);
//            case "LINEAR_REGRESSION":
//            default:
//                return calculateLinearRegression(historicalData);
//        }
//    }
//
//    private ForecastResult calculateLinearRegression(List<HistoricalSalesData> data) {
//        int n = data.size();
//        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
//
//        for (int i = 0; i < n; i++) {
//            double x = i + 1;
//            double y = data.get(i).getQuantity();
//            sumX += x;
//            sumY += y;
//            sumXY += x * y;
//            sumX2 += x * x;
//        }
//
//        // Calculate slope (m) and intercept (b) for y = mx + b
//        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
//        double intercept = (sumY - slope * sumX) / n;
//
//        // Predict next value
//        int nextX = n + 1;
//        int predictedDemand = (int) Math.round(slope * nextX + intercept);
//
//        // Calculate confidence based on R-squared
//        double meanY = sumY / n;
//        double ssTotal = 0, ssRes = 0;
//        for (int i = 0; i < n; i++) {
//            double x = i + 1;
//            double y = data.get(i).getQuantity();
//            double predicted = slope * x + intercept;
//            ssTotal += Math.pow(y - meanY, 2);
//            ssRes += Math.pow(y - predicted, 2);
//        }
//        double rSquared = 1 - (ssRes / ssTotal);
//        BigDecimal confidenceScore = BigDecimal.valueOf(rSquared * 100)
//                .setScale(2, RoundingMode.HALF_UP);
//
//        // Calculate trend
//        BigDecimal trendFactor = BigDecimal.valueOf(slope)
//                .setScale(2, RoundingMode.HALF_UP);
//
//        return ForecastResult.builder()
//                .predictedDemand(Math.max(0, predictedDemand))
//                .confidenceScore(confidenceScore)
//                .method("LINEAR_REGRESSION")
//                .trendFactor(trendFactor)
//                .seasonalityFactor(BigDecimal.ONE)
//                .marketGrowthRate(BigDecimal.ZERO)
//                .build();
//    }
//
//    private ForecastResult calculateMovingAverage(List<HistoricalSalesData> data) {
//        int windowSize = Math.min(3, data.size());
//        double sum = 0;
//
//        for (int i = data.size() - windowSize; i < data.size(); i++) {
//            sum += data.get(i).getQuantity();
//        }
//
//        int predictedDemand = (int) Math.round(sum / windowSize);
//
//        return ForecastResult.builder()
//                .predictedDemand(predictedDemand)
//                .confidenceScore(BigDecimal.valueOf(70))
//                .method("MOVING_AVERAGE")
//                .trendFactor(BigDecimal.ZERO)
//                .seasonalityFactor(BigDecimal.ONE)
//                .marketGrowthRate(BigDecimal.ZERO)
//                .build();
//    }
//
//    private ForecastResult calculateExponentialSmoothing(List<HistoricalSalesData> data) {
//        double alpha = 0.3; // Smoothing parameter
//        double forecast = data.get(0).getQuantity();
//
//        for (int i = 1; i < data.size(); i++) {
//            forecast = alpha * data.get(i).getQuantity() + (1 - alpha) * forecast;
//        }
//
//        return ForecastResult.builder()
//                .predictedDemand((int) Math.round(forecast))
//                .confidenceScore(BigDecimal.valueOf(75))
//                .method("EXPONENTIAL_SMOOTHING")
//                .trendFactor(BigDecimal.ZERO)
//                .seasonalityFactor(BigDecimal.ONE)
//                .marketGrowthRate(BigDecimal.ZERO)
//                .build();
//    }
//
//    private int calculateAccuracy(Integer predicted, Integer actual) {
//        if (actual == null || actual == 0) return 0;
//        double error = Math.abs(actual - predicted);
//        return (int) Math.round((1 - error / actual) * 100);
//    }
//
//    private DemandForecastResponse convertToResponse(DemandForecast f) {
//        Integer accuracy = null;
//        if (f.getActualDemand() != null) {
//            accuracy = calculateAccuracy(f.getPredictedDemand(), f.getActualDemand());
//        }
//
//        // Generate insights
//        DemandForecastResponse.ForecastInsights insights = generateInsights(f);
//
//        DemandForecastResponse.DemandForecastResponseBuilder builder = DemandForecastResponse.builder()
//                .id(f.getId())
//                .forecastPeriod(f.getForecastPeriod())
//                .forecastDate(f.getForecastDate())
//                .predictedDemand(f.getPredictedDemand())
//                .confidenceScore(f.getConfidenceScore())
//                .actualDemand(f.getActualDemand())
//                .accuracy(accuracy)
//                .forecastMethod(f.getForecastMethod())
//                .historicalDataPoints(f.getHistoricalDataPoints())
//                .seasonalityFactor(f.getSeasonalityFactor())
//                .trendFactor(f.getTrendFactor())
//                .marketGrowthRate(f.getMarketGrowthRate())
//                .status(f.getStatus())
//                .notes(f.getNotes())
//                .createdAt(f.getCreatedAt())
//                .updatedAt(f.getUpdatedAt())
//                .insights(insights);
//
//        if (f.getProduct() != null) {
//            builder.productId(f.getProduct().getId())
//                    .productName(f.getProduct().getProductName())
//                    .productVersion(f.getProduct().getVersion());
//
//            if (f.getProduct().getBrand() != null) {
//                builder.brandName(f.getProduct().getBrand().getBrandName());
//            }
//        }
//
//        if (f.getCreatedBy() != null) {
//            builder.createdByName(f.getCreatedBy().getFullName());
//        }
//
//        return builder.build();
//    }
//
//    private DemandForecastResponse.ForecastInsights generateInsights(DemandForecast f) {
//        String trend = "STABLE";
//        BigDecimal trendPercentage = BigDecimal.ZERO;
//
//        if (f.getTrendFactor() != null) {
//            if (f.getTrendFactor().compareTo(BigDecimal.valueOf(5)) > 0) {
//                trend = "INCREASING";
//                trendPercentage = f.getTrendFactor();
//            } else if (f.getTrendFactor().compareTo(BigDecimal.valueOf(-5)) < 0) {
//                trend = "DECREASING";
//                trendPercentage = f.getTrendFactor().abs();
//            }
//        }
//
//        List<String> factors = new ArrayList<>();
//        factors.add("Xu hướng thị trường: " + trend);
//        if (f.getSeasonalityFactor() != null && f.getSeasonalityFactor().compareTo(BigDecimal.ONE) != 0) {
//            factors.add("Có yếu tố mùa vụ");
//        }
//        if (f.getHistoricalDataPoints() != null) {
//            factors.add("Dựa trên " + f.getHistoricalDataPoints() + " điểm dữ liệu");
//        }
//
//        String recommendation = generateRecommendation(f, trend);
//
//        return DemandForecastResponse.ForecastInsights.builder()
//                .trend(trend)
//                .trendPercentage(trendPercentage)
//                .seasonalPattern("NORMAL")
//                .influencingFactors(factors)
//                .recommendation(recommendation)
//                .build();
//    }
//
//    private String generateRecommendation(DemandForecast f, String trend) {
//        if ("INCREASING".equals(trend)) {
//            return "Nên tăng sản xuất và dự trữ để đáp ứng nhu cầu tăng cao";
//        } else if ("DECREASING".equals(trend)) {
//            return "Cân nhắc giảm sản xuất và chạy chương trình khuyến mãi";
//        }
//        return "Duy trì mức sản xuất hiện tại";
//    }
//
//    private Pageable buildPageable(ForecastFilterRequest req) {
//        int page = req.getPage() != null ? req.getPage() : 0;
//        int size = req.getSize() != null ? req.getSize() : 20;
//        if (size > 100) size = 100;
//
//        Sort sort = Sort.by(Sort.Direction.DESC, "forecastDate");
//        if (req.getSortBy() != null) {
//            switch (req.getSortBy()) {
//                case "forecast_date_asc": sort = Sort.by(Sort.Direction.ASC, "forecastDate"); break;
//                case "confidence_desc": sort = Sort.by(Sort.Direction.DESC, "confidenceScore"); break;
//                case "predicted_desc": sort = Sort.by(Sort.Direction.DESC, "predictedDemand"); break;
//            }
//        }
//
//        return PageRequest.of(page, size, sort);
//    }
//
//    // Inner classes
//    @lombok.Data
//    @lombok.AllArgsConstructor
//    private static class HistoricalSalesData {
//        private LocalDate date;
//        private int quantity;
//    }
//
//    @lombok.Data
//    @lombok.Builder
//    private static class ForecastResult {
//        private int predictedDemand;
//        private BigDecimal confidenceScore;
//        private String method;
//        private BigDecimal trendFactor;
//        private BigDecimal seasonalityFactor;
//        private BigDecimal marketGrowthRate;
//    }
//}
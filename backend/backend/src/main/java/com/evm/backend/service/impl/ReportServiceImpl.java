package com.evm.backend.service.impl;

import com.evm.backend.dto.request.ReportFilterRequest;
import com.evm.backend.dto.response.*;
import com.evm.backend.entity.DealerContract;
import com.evm.backend.entity.Inventory;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final InventoryReportRepository inventoryRepository;
    private final DealerContractRepository contractRepository;
    private final DealerRepository dealerRepository;

    // =====================================================
    // D.1: SALES REPORT - FIXED
    // =====================================================

    @Override
    public SalesReportResponse getSalesReport(ReportFilterRequest filter) {
        log.info("Generating sales report from {} to {}",
                filter.getStartDate(), filter.getEndDate());

        LocalDate startDate = filter.getStartDate() != null ?
                filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate endDate = filter.getEndDate() != null ?
                filter.getEndDate() : LocalDate.now();

        // ✅ Get sales data by period (returns Object[])
        List<Object[]> salesData = reportRepository.getSalesByDay(
                startDate, endDate, filter.getDealerId());

        // ✅ Convert Object[] to SalesDataPoint
        List<SalesReportResponse.SalesDataPoint> salesByPeriod = salesData.stream()
                .map(data -> SalesReportResponse.SalesDataPoint.builder()
                        .date((LocalDate) data[0])
                        .period(data[0].toString())
                        .revenue(convertToBigDecimal(data[1]))      // ✅ FIX Ở ĐÂY
                        .orderCount(((Number) data[2]).longValue())
                        .averageValue(convertToBigDecimal(data[3])) // ✅ VÀ ĐÂY
                        .build())
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal totalRevenue = reportRepository.getTotalRevenue(
                startDate, endDate, filter.getDealerId());
        Long totalOrders = reportRepository.getTotalOrders(
                startDate, endDate, filter.getDealerId());
        BigDecimal totalDiscount = reportRepository.getTotalDiscount(
                startDate, endDate, filter.getDealerId());

        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Calculate growth rate
        LocalDate prevStartDate = startDate.minusDays(
                endDate.toEpochDay() - startDate.toEpochDay());
        BigDecimal previousRevenue = reportRepository.getTotalRevenue(
                prevStartDate, startDate.minusDays(1), filter.getDealerId());
        BigDecimal growthRate = calculateGrowthRate(totalRevenue, previousRevenue);

        // ✅ Get top performers (returns Object[])
        List<SalesReportResponse.TopPerformer> topSalesPersons =
                convertToTopPerformers(reportRepository.getTopSalesPersons(startDate, endDate))
                        .stream().limit(10).collect(Collectors.toList());

        List<SalesReportResponse.TopPerformer> topDealers =
                convertToTopPerformers(reportRepository.getTopDealers(startDate, endDate))
                        .stream().limit(10).collect(Collectors.toList());

        // ✅ Get top products
        List<SalesReportResponse.TopProduct> topProducts =
                convertToTopProducts(reportRepository.getTopProducts(startDate, endDate))
                        .stream().limit(10).collect(Collectors.toList());

        return SalesReportResponse.builder()
                .fromDate(startDate)
                .toDate(endDate)
                .groupBy(filter.getGroupBy() != null ? filter.getGroupBy() : "DAY")
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .totalDiscount(totalDiscount)
                .growthRate(growthRate)
                .salesByPeriod(salesByPeriod)
                .topSalesPersons(topSalesPersons)
                .topDealers(topDealers)
                .topProducts(topProducts)
                .build();
    }

    // =====================================================
    // D.2: INVENTORY REPORT - FIXED
    // =====================================================

    @Override
    public InventoryReportResponse getInventoryReport(ReportFilterRequest filter) {
        log.info("Generating inventory report");

        // Get inventory details (returns Object[])
        List<Object[]> inventoryData = inventoryRepository.getInventoryReportData(
                filter.getProductId(), filter.getDealerId());

        // Convert to InventoryDetail - SAFE CASTING
        List<InventoryReportResponse.InventoryDetail> inventoryDetails = inventoryData.stream()
                .map(data -> {
                    // Sử dụng convertToNumber để đảm bảo ép kiểu an toàn
                    Long productId = convertToNumber(data[0], Long.class);
                    String productName = data[1] != null ? (String) data[1] : "";
                    String version = data[2] != null ? (String) data[2] : "";
                    Long dealerId = convertToNumber(data[3], Long.class);
                    String dealerName = data[4] != null ? (String) data[4] : "";
                    String location = data[5] != null ? (String) data[5] : "";
                    Integer totalQty = convertToNumber(data[6], Integer.class);
                    Integer availableQty = convertToNumber(data[7], Integer.class);
                    Integer reservedQty = convertToNumber(data[8], Integer.class);
                    Integer inTransitQty = convertToNumber(data[9], Integer.class);
                    String stockStatus = data[10] != null ? (String) data[10] : "UNKNOWN";

                    return InventoryReportResponse.InventoryDetail.builder()
                            .productId(productId)
                            .productName(productName)
                            .version(version)
                            .dealerId(dealerId)
                            .dealerName(dealerName)
                            .location(location)
                            .totalQuantity(totalQty)
                            .availableQuantity(availableQty)
                            .reservedQuantity(reservedQty)
                            .inTransitQuantity(inTransitQty)
                            .stockStatus(stockStatus)
                            .build();
                })
                .collect(Collectors.toList());

        // Get statistics
        List<Object[]> statsList = inventoryRepository.getInventoryStatistics();

        // SỬA: Lấy mảng Object[] đầu tiên (hàng dữ liệu thống kê)
        Object[] statsData = (statsList != null && !statsList.isEmpty()) ?
                statsList.get(0) :
                new Object[]{0L, 0, 0, 0, 0}; // Dùng Long/Integer để tránh lỗi ép kiểu ban đầu

        // Truy cập các cột trong mảng statsData (index 0 đến 4)
        Integer totalProducts = convertToNumber(statsData[0], Integer.class);
        Integer totalStock = convertToNumber(statsData[1], Integer.class);
        Integer availableStock = convertToNumber(statsData[2], Integer.class);
        Integer reservedStock = convertToNumber(statsData[3], Integer.class);
        Integer inTransitStock = convertToNumber(statsData[4], Integer.class);

        // Count alerts
        int lowStockCount = (int) inventoryDetails.stream()
                .filter(d -> "LOW_STOCK".equals(d.getStockStatus()))
                .count();
        int outOfStockCount = (int) inventoryDetails.stream()
                .filter(d -> "OUT_OF_STOCK".equals(d.getStockStatus()))
                .count();

        // Generate alerts
        List<InventoryReportResponse.StockAlert> alerts = generateStockAlerts();

        return InventoryReportResponse.builder()
                .reportDate(LocalDate.now())
                .totalProducts(totalProducts)
                .totalStock(totalStock)
                .availableStock(availableStock)
                .reservedStock(reservedStock)
                .inTransitStock(inTransitStock)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .inventoryDetails(inventoryDetails)
                .alerts(alerts)
                .build();
    }

    // =====================================================
    // C.1, C.2: DEALER PERFORMANCE - FIXED
    // =====================================================

    @Override
    public DealerPerformanceResponse getDealerPerformance(
            Long dealerId, ReportFilterRequest filter) {
        log.info("Generating performance report for dealer: {}", dealerId);

        var dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        LocalDate startDate = filter.getStartDate() != null ?
                filter.getStartDate() : LocalDate.now().minusMonths(3);
        LocalDate endDate = filter.getEndDate() != null ?
                filter.getEndDate() : LocalDate.now();

        // Get sales data
        BigDecimal totalRevenue = reportRepository.getDealerTotalSales(
                dealerId, startDate, endDate);
        Long totalOrders = reportRepository.getTotalOrders(startDate, endDate, dealerId);
        BigDecimal avgOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Get contract and KPI
        DealerContract contract = contractRepository
                .findActiveContract(dealerId, LocalDate.now())
                .orElse(null);

        BigDecimal salesTarget = contract != null ? contract.getSalesTarget() : BigDecimal.ZERO;
        BigDecimal achievementRate = calculateAchievementRate(totalRevenue, salesTarget);
        String performanceLevel = determinePerformanceLevel(achievementRate);

        // ✅ Get monthly breakdown (returns Object[])
        List<DealerPerformanceResponse.MonthlyPerformance> monthlyPerformance =
                getMonthlyPerformance(dealerId, startDate, endDate, salesTarget);

        // ✅ Get product breakdown (returns Object[])
        List<DealerPerformanceResponse.ProductPerformance> productBreakdown =
                getProductBreakdown(dealerId, startDate, endDate, totalRevenue);

        return DealerPerformanceResponse.builder()
                .dealerId(dealerId)
                .dealerName(dealer.getDealerName())
                .fromDate(startDate)
                .toDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .salesTarget(salesTarget)
                .achievedSales(totalRevenue)
                .achievementRate(achievementRate)
                .performanceLevel(performanceLevel)
                .contractStartDate(contract != null ? contract.getStartDate() : null)
                .contractEndDate(contract != null ? contract.getEndDate() : null)
                .commissionRate(contract != null ? contract.getCommissionRate() : null)
                .monthlyPerformance(monthlyPerformance)
                .productBreakdown(productBreakdown)
                .build();
    }

    @Override
    public List<DealerPerformanceResponse> getAllDealersPerformance(
            ReportFilterRequest filter) {
        log.info("Generating performance report for all dealers");

        var dealers = dealerRepository.findAll();
        return dealers.stream()
                .map(dealer -> getDealerPerformance(dealer.getId(), filter))
                .collect(Collectors.toList());
    }

    // =====================================================
    // REVENUE REPORT
    // =====================================================

    @Override
    public RevenueReportResponse getRevenueReport(ReportFilterRequest filter) {
        log.info("Generating revenue report");

        LocalDate startDate = filter.getStartDate() != null ?
                filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate endDate = filter.getEndDate() != null ?
                filter.getEndDate() : LocalDate.now();

        BigDecimal totalRevenue = reportRepository.getTotalRevenue(
                startDate, endDate, null);
        BigDecimal totalPaid = reportRepository.getTotalPaidAmount(startDate, endDate);

        // Calculate previous period for growth
        LocalDate prevStartDate = startDate.minusDays(
                endDate.toEpochDay() - startDate.toEpochDay());
        BigDecimal previousRevenue = reportRepository.getTotalRevenue(
                prevStartDate, startDate.minusDays(1), null);
        BigDecimal growthRate = calculateGrowthRate(totalRevenue, previousRevenue);

        return RevenueReportResponse.builder()
                .reportDate(LocalDate.now())
                .fromDate(startDate)
                .toDate(endDate)
                .totalRevenue(totalRevenue)
                .totalPaid(totalPaid)
                .totalPending(totalRevenue.subtract(totalPaid))
                .previousPeriodRevenue(previousRevenue)
                .growthRate(growthRate)
                .build();
    }

    // =====================================================
    // EXPORT FUNCTIONS (Placeholders)
    // =====================================================

    @Override
    public byte[] exportSalesReportToExcel(ReportFilterRequest filter) {
        throw new UnsupportedOperationException("Excel export not implemented yet");
    }

    @Override
    public byte[] exportInventoryReportToExcel(ReportFilterRequest filter) {
        throw new UnsupportedOperationException("Excel export not implemented yet");
    }

    @Override
    public byte[] exportDealerPerformanceToPdf(Long dealerId, ReportFilterRequest filter) {
        throw new UnsupportedOperationException("PDF export not implemented yet");
    }

    // =====================================================
    // HELPER METHODS - FIXED
    // =====================================================

    /**
     * ✅ Convert Object[] to TopPerformer
     */
    private List<SalesReportResponse.TopPerformer> convertToTopPerformers(List<Object[]> data) {
        return data.stream()
                .map(row -> SalesReportResponse.TopPerformer.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .totalRevenue(convertToBigDecimal(row[2]))  // ✅ FIX Ở ĐÂY
                        .orderCount(((Number) row[3]).longValue())
                        .achievementRate(BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }
    /**
     * ✅ Convert Object[] to TopProduct
     */
    private List<SalesReportResponse.TopProduct> convertToTopProducts(List<Object[]> data) {
        return data.stream()
                .map(row -> SalesReportResponse.TopProduct.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .unitsSold(((Number) row[2]).longValue())
                        .revenue(convertToBigDecimal(row[3]))  // ✅ FIX Ở ĐÂY
                        .build())
                .collect(Collectors.toList());
    }

    private <T extends Number> T convertToNumber(Object value, Class<T> targetType) {
        if (value == null) {
            return targetType.cast(0);
        }
        // Chuyển đổi an toàn sang BigDecimal trước
        BigDecimal bd = convertToBigDecimal(value);

        if (targetType.equals(Long.class) || targetType.equals(Long.TYPE)) {
            return targetType.cast(bd.longValue());
        }
        if (targetType.equals(Integer.class) || targetType.equals(Integer.TYPE)) {
            return targetType.cast(bd.intValue());
        }
        // Fallback for other Number types
        return targetType.cast(bd.longValue());
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) { // Bổ sung xử lý String
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse String to BigDecimal: {}", value);
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateAchievementRate(BigDecimal achieved, BigDecimal target) {
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return achieved.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private String determinePerformanceLevel(BigDecimal achievementRate) {
        if (achievementRate.compareTo(new BigDecimal("100")) >= 0) {
            return "EXCELLENT";
        } else if (achievementRate.compareTo(new BigDecimal("80")) >= 0) {
            return "GOOD";
        } else if (achievementRate.compareTo(new BigDecimal("60")) >= 0) {
            return "AVERAGE";
        } else {
            return "POOR";
        }
    }

    /**
     * ✅ Get monthly performance (convert Object[] to DTO)
     */
    private List<DealerPerformanceResponse.MonthlyPerformance> getMonthlyPerformance(
            Long dealerId, LocalDate startDate, LocalDate endDate, BigDecimal totalTarget) {

        List<Object[]> monthlyData = reportRepository
                .getDealerSalesByMonth(dealerId, startDate, endDate);

        int monthCount = (int) java.time.temporal.ChronoUnit.MONTHS
                .between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1)) + 1;
        BigDecimal monthlyTarget = totalTarget.divide(
                BigDecimal.valueOf(monthCount), 2, RoundingMode.HALF_UP);

        return monthlyData.stream()
                .map(data -> {
                    String month = (String) data[0];
                    BigDecimal revenue = convertToBigDecimal(data[1]);  // ✅ FIX Ở ĐÂY
                    Long orderCount = ((Number) data[2]).longValue();
                    BigDecimal achievement = calculateAchievementRate(revenue, monthlyTarget);

                    return DealerPerformanceResponse.MonthlyPerformance.builder()
                            .month(month)
                            .revenue(revenue)
                            .orderCount(orderCount)
                            .target(monthlyTarget)
                            .achievementRate(achievement)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get product breakdown (convert Object[] to DTO)
     */
    private List<DealerPerformanceResponse.ProductPerformance> getProductBreakdown(
            Long dealerId, LocalDate startDate, LocalDate endDate, BigDecimal totalRevenue) {

        List<Object[]> productData = reportRepository
                .getDealerProductBreakdown(dealerId, startDate, endDate);

        return productData.stream()
                .map(data -> {
                    Long productId = ((Number) data[0]).longValue();
                    String productName = (String) data[1];
                    Long unitsSold = ((Number) data[2]).longValue();
                    BigDecimal revenue = convertToBigDecimal(data[3]);

                    BigDecimal percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal("100")) :
                            BigDecimal.ZERO;

                    return DealerPerformanceResponse.ProductPerformance.builder()
                            .productId(productId)
                            .productName(productName)
                            .unitsSold(unitsSold)
                            .revenue(revenue)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<InventoryReportResponse.StockAlert> generateStockAlerts() {
        List<InventoryReportResponse.StockAlert> alerts = new ArrayList<>();

        // Low stock alerts
        List<Inventory> lowStock = inventoryRepository.getLowStockItems();
        for (Inventory inv : lowStock) {
            alerts.add(InventoryReportResponse.StockAlert.builder()
                    .productId(inv.getProduct().getId())
                    .productName(inv.getProduct().getProductName())
                    .dealerId(inv.getDealer() != null ? inv.getDealer().getId() : null)
                    .dealerName(inv.getDealer() != null ? inv.getDealer().getDealerName() : "Brand Warehouse")
                    .alertType("LOW_STOCK")
                    .currentQuantity(inv.getAvailableQuantity())
                    .minStockLevel(5)
                    .message("Số lượng tồn kho thấp, cần nhập thêm hàng")
                    .build());
        }

        // Out of stock alerts
        List<Inventory> outOfStock = inventoryRepository.getOutOfStockItems();
        for (Inventory inv : outOfStock) {
            alerts.add(InventoryReportResponse.StockAlert.builder()
                    .productId(inv.getProduct().getId())
                    .productName(inv.getProduct().getProductName())
                    .dealerId(inv.getDealer() != null ? inv.getDealer().getId() : null)
                    .dealerName(inv.getDealer() != null ? inv.getDealer().getDealerName() : "Brand Warehouse")
                    .alertType("OUT_OF_STOCK")
                    .currentQuantity(0)
                    .minStockLevel(5)
                    .message("Hết hàng - Cần nhập hàng khẩn cấp")
                    .build());
        }

        return alerts;
    }
}
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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

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
        log.info("Exporting sales report to Excel");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Get report data
            SalesReportResponse report = getSalesReport(filter);

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);

            // ========== SHEET 1: SUMMARY ==========
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createSalesSummarySheet(summarySheet, report, headerStyle, currencyStyle, percentStyle);

            // ========== SHEET 2: SALES BY PERIOD ==========
            Sheet periodSheet = workbook.createSheet("Doanh thu theo thời gian");
            createSalesByPeriodSheet(periodSheet, report, headerStyle, dateStyle, currencyStyle, numberStyle);

            // ========== SHEET 3: TOP PERFORMERS ==========
            Sheet performersSheet = workbook.createSheet("Top nhân viên & đại lý");
            createTopPerformersSheet(performersSheet, report, headerStyle, currencyStyle, numberStyle);

            // ========== SHEET 4: TOP PRODUCTS ==========
            Sheet productsSheet = workbook.createSheet("Top sản phẩm");
            createTopProductsSheet(productsSheet, report, headerStyle, currencyStyle, numberStyle);

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < 10; j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            workbook.write(outputStream);
            log.info("Sales report Excel exported successfully");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting sales report to Excel", e);
            throw new RuntimeException("Failed to export sales report", e);
        }
    }

    @Override
    public byte[] exportInventoryReportToExcel(ReportFilterRequest filter) {
        log.info("Exporting inventory report to Excel");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Get report data
            InventoryReportResponse report = getInventoryReport(filter);

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle alertStyle = createAlertStyle(workbook);

            // ========== SHEET 1: SUMMARY ==========
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createInventorySummarySheet(summarySheet, report, headerStyle, numberStyle);

            // ========== SHEET 2: INVENTORY DETAILS ==========
            Sheet detailsSheet = workbook.createSheet("Chi tiết tồn kho");
            createInventoryDetailsSheet(detailsSheet, report, headerStyle, numberStyle);

            // ========== SHEET 3: ALERTS ==========
            Sheet alertsSheet = workbook.createSheet("Cảnh báo");
            createInventoryAlertsSheet(alertsSheet, report, headerStyle, alertStyle, numberStyle);

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < 15; j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            workbook.write(outputStream);
            log.info("Inventory report Excel exported successfully");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting inventory report to Excel", e);
            throw new RuntimeException("Failed to export inventory report", e);
        }
    }

    @Override
    public byte[] exportDealerPerformanceToPdf(Long dealerId, ReportFilterRequest filter) {
        log.info("Exporting dealer performance to Excel for dealer: {}", dealerId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Get report data
            DealerPerformanceResponse report = getDealerPerformance(dealerId, filter);

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);

            // ========== SHEET 1: OVERVIEW ==========
            Sheet overviewSheet = workbook.createSheet("Tổng quan");
            createDealerOverviewSheet(overviewSheet, report, headerStyle, currencyStyle, percentStyle);

            // ========== SHEET 2: MONTHLY PERFORMANCE ==========
            Sheet monthlySheet = workbook.createSheet("Hiệu suất theo tháng");
            createMonthlyPerformanceSheet(monthlySheet, report, headerStyle, currencyStyle, numberStyle, percentStyle);

            // ========== SHEET 3: PRODUCT BREAKDOWN ==========
            Sheet productSheet = workbook.createSheet("Phân tích sản phẩm");
            createProductBreakdownSheet(productSheet, report, headerStyle, currencyStyle, numberStyle, percentStyle);

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < 10; j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            workbook.write(outputStream);
            log.info("Dealer performance Excel exported successfully");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting dealer performance to Excel", e);
            throw new RuntimeException("Failed to export dealer performance", e);
        }
    }

// ============================================================
// HELPER METHODS - EXCEL STYLES
// ============================================================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0 \"₫\""));
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00\"%\""));
        return style;
    }

    private CellStyle createAlertStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

// ============================================================
// HELPER METHODS - SALES REPORT SHEETS
// ============================================================

    private void createSalesSummarySheet(Sheet sheet, SalesReportResponse report,
                                         CellStyle headerStyle, CellStyle currencyStyle,
                                         CellStyle percentStyle) {
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU");

        rowNum++; // Empty row

        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Từ ngày:");
        periodRow.createCell(1).setCellValue(report.getFromDate().format(DATE_FORMATTER));
        periodRow.createCell(2).setCellValue("Đến ngày:");
        periodRow.createCell(3).setCellValue(report.getToDate().format(DATE_FORMATTER));

        rowNum++; // Empty row

        // Summary data
        createSummaryRow(sheet, rowNum++, "Tổng doanh thu:",
                report.getTotalRevenue(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tổng đơn hàng:",
                report.getTotalOrders(), null);
        createSummaryRow(sheet, rowNum++, "Giá trị TB/đơn:",
                report.getAverageOrderValue(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tổng giảm giá:",
                report.getTotalDiscount(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tăng trưởng:",
                report.getGrowthRate(), percentStyle);
    }

    private void createSalesByPeriodSheet(Sheet sheet, SalesReportResponse report,
                                          CellStyle headerStyle, CellStyle dateStyle,
                                          CellStyle currencyStyle, CellStyle numberStyle) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Ngày", "Doanh thu", "Số đơn", "Giá trị TB"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        for (SalesReportResponse.SalesDataPoint data : report.getSalesByPeriod()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getDate().format(DATE_FORMATTER));

            Cell revenueCell = row.createCell(1);
            revenueCell.setCellValue(data.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);

            Cell countCell = row.createCell(2);
            countCell.setCellValue(data.getOrderCount());
            countCell.setCellStyle(numberStyle);

            Cell avgCell = row.createCell(3);
            avgCell.setCellValue(data.getAverageValue().doubleValue());
            avgCell.setCellStyle(currencyStyle);
        }
    }

    private void createTopPerformersSheet(Sheet sheet, SalesReportResponse report,
                                          CellStyle headerStyle, CellStyle currencyStyle,
                                          CellStyle numberStyle) {
        int rowNum = 0;

        // Top Sales Persons
        Row titleRow1 = sheet.createRow(rowNum++);
        titleRow1.createCell(0).setCellValue("TOP NHÂN VIÊN BÁN HÀNG");
        rowNum++;

        Row headerRow1 = sheet.createRow(rowNum++);
        String[] headers1 = {"#", "Tên", "Doanh thu", "Số đơn"};
        for (int i = 0; i < headers1.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(headers1[i]);
            cell.setCellStyle(headerStyle);
        }

        int rank = 1;
        for (SalesReportResponse.TopPerformer performer : report.getTopSalesPersons()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(performer.getName());

            Cell revenueCell = row.createCell(2);
            revenueCell.setCellValue(performer.getTotalRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);

            Cell countCell = row.createCell(3);
            countCell.setCellValue(performer.getOrderCount());
            countCell.setCellStyle(numberStyle);
        }

        rowNum += 2; // Empty rows

        // Top Dealers
        Row titleRow2 = sheet.createRow(rowNum++);
        titleRow2.createCell(0).setCellValue("TOP ĐẠI LÝ");
        rowNum++;

        Row headerRow2 = sheet.createRow(rowNum++);
        for (int i = 0; i < headers1.length; i++) {
            Cell cell = headerRow2.createCell(i);
            cell.setCellValue(headers1[i]);
            cell.setCellStyle(headerStyle);
        }

        rank = 1;
        for (SalesReportResponse.TopPerformer dealer : report.getTopDealers()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(dealer.getName());

            Cell revenueCell = row.createCell(2);
            revenueCell.setCellValue(dealer.getTotalRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);

            Cell countCell = row.createCell(3);
            countCell.setCellValue(dealer.getOrderCount());
            countCell.setCellStyle(numberStyle);
        }
    }

    private void createTopProductsSheet(Sheet sheet, SalesReportResponse report,
                                        CellStyle headerStyle, CellStyle currencyStyle,
                                        CellStyle numberStyle) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"#", "Sản phẩm", "Số lượng bán", "Doanh thu"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rank = 1;
        for (SalesReportResponse.TopProduct product : report.getTopProducts()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(product.getProductName());

            Cell unitsCell = row.createCell(2);
            unitsCell.setCellValue(product.getUnitsSold());
            unitsCell.setCellStyle(numberStyle);

            Cell revenueCell = row.createCell(3);
            revenueCell.setCellValue(product.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
        }
    }

// ============================================================
// HELPER METHODS - INVENTORY REPORT SHEETS
// ============================================================

    private void createInventorySummarySheet(Sheet sheet, InventoryReportResponse report,
                                             CellStyle headerStyle, CellStyle numberStyle) {
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("BÁO CÁO TỒN KHO");
        rowNum++;

        // Date
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Ngày báo cáo:");
        dateRow.createCell(1).setCellValue(report.getReportDate().format(DATE_FORMATTER));
        rowNum++;

        // Summary
        createSummaryRow(sheet, rowNum++, "Tổng số sản phẩm:", report.getTotalProducts(), numberStyle);
        createSummaryRow(sheet, rowNum++, "Tổng tồn kho:", report.getTotalStock(), numberStyle);
        createSummaryRow(sheet, rowNum++, "Có sẵn:", report.getAvailableStock(), numberStyle);
        createSummaryRow(sheet, rowNum++, "Đã đặt:", report.getReservedStock(), numberStyle);
        createSummaryRow(sheet, rowNum++, "Đang vận chuyển:", report.getInTransitStock(), numberStyle);
        createSummaryRow(sheet, rowNum++, "Cảnh báo hết hàng:", report.getOutOfStockCount(), numberStyle);
        createSummaryRow(sheet, rowNum++, "Cảnh báo tồn thấp:", report.getLowStockCount(), numberStyle);
    }

    private void createInventoryDetailsSheet(Sheet sheet, InventoryReportResponse report,
                                             CellStyle headerStyle, CellStyle numberStyle) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Sản phẩm", "Phiên bản", "Đại lý", "Vị trí",
                "Tổng", "Có sẵn", "Đã đặt", "Đang chuyển", "Trạng thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        for (InventoryReportResponse.InventoryDetail detail : report.getInventoryDetails()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detail.getProductName());
            row.createCell(1).setCellValue(detail.getVersion());
            row.createCell(2).setCellValue(detail.getDealerName());
            row.createCell(3).setCellValue(detail.getLocation());

            createNumberCell(row, 4, detail.getTotalQuantity(), numberStyle);
            createNumberCell(row, 5, detail.getAvailableQuantity(), numberStyle);
            createNumberCell(row, 6, detail.getReservedQuantity(), numberStyle);
            createNumberCell(row, 7, detail.getInTransitQuantity(), numberStyle);

            row.createCell(8).setCellValue(detail.getStockStatus());
        }
    }

    private void createInventoryAlertsSheet(Sheet sheet, InventoryReportResponse report,
                                            CellStyle headerStyle, CellStyle alertStyle,
                                            CellStyle numberStyle) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Loại", "Sản phẩm", "Đại lý", "SL hiện tại", "SL tối thiểu", "Thông báo"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        for (InventoryReportResponse.StockAlert alert : report.getAlerts()) {
            Row row = sheet.createRow(rowNum++);

            Cell typeCell = row.createCell(0);
            typeCell.setCellValue(alert.getAlertType());
            if ("OUT_OF_STOCK".equals(alert.getAlertType())) {
                typeCell.setCellStyle(alertStyle);
            }

            row.createCell(1).setCellValue(alert.getProductName());
            row.createCell(2).setCellValue(alert.getDealerName());
            createNumberCell(row, 3, alert.getCurrentQuantity(), numberStyle);
            createNumberCell(row, 4, alert.getMinStockLevel(), numberStyle);
            row.createCell(5).setCellValue(alert.getMessage());
        }
    }

// ============================================================
// HELPER METHODS - DEALER PERFORMANCE SHEETS
// ============================================================

    private void createDealerOverviewSheet(Sheet sheet, DealerPerformanceResponse report,
                                           CellStyle headerStyle, CellStyle currencyStyle,
                                           CellStyle percentStyle) {
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("BÁO CÁO HIỆU SUẤT ĐẠI LÝ");
        rowNum++;

        // Dealer info
        Row dealerRow = sheet.createRow(rowNum++);
        dealerRow.createCell(0).setCellValue("Đại lý:");
        dealerRow.createCell(1).setCellValue(report.getDealerName());
        rowNum++;

        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Từ ngày:");
        periodRow.createCell(1).setCellValue(report.getFromDate().format(DATE_FORMATTER));
        periodRow.createCell(2).setCellValue("Đến ngày:");
        periodRow.createCell(3).setCellValue(report.getToDate().format(DATE_FORMATTER));
        rowNum++;

        // Performance data
        createSummaryRow(sheet, rowNum++, "Doanh thu:", report.getTotalRevenue(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Mục tiêu:", report.getSalesTarget(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tỷ lệ đạt:", report.getAchievementRate(), percentStyle);
        createSummaryRow(sheet, rowNum++, "Xếp loại:", report.getPerformanceLevel(), null);
        createSummaryRow(sheet, rowNum++, "Tổng đơn:", report.getTotalOrders(), null);
        createSummaryRow(sheet, rowNum++, "Giá trị TB:", report.getAverageOrderValue(), currencyStyle);
    }

    private void createMonthlyPerformanceSheet(Sheet sheet, DealerPerformanceResponse report,
                                               CellStyle headerStyle, CellStyle currencyStyle,
                                               CellStyle numberStyle, CellStyle percentStyle) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Tháng", "Doanh thu", "Mục tiêu", "Số đơn", "Tỷ lệ đạt"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        for (DealerPerformanceResponse.MonthlyPerformance monthly : report.getMonthlyPerformance()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(monthly.getMonth());

            Cell revenueCell = row.createCell(1);
            revenueCell.setCellValue(monthly.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);

            Cell targetCell = row.createCell(2);
            targetCell.setCellValue(monthly.getTarget().doubleValue());
            targetCell.setCellStyle(currencyStyle);

            Cell countCell = row.createCell(3);
            countCell.setCellValue(monthly.getOrderCount());
            countCell.setCellStyle(numberStyle);

            Cell achievementCell = row.createCell(4);
            achievementCell.setCellValue(monthly.getAchievementRate().doubleValue() / 100);
            achievementCell.setCellStyle(percentStyle);
        }
    }

    private void createProductBreakdownSheet(Sheet sheet, DealerPerformanceResponse report,
                                             CellStyle headerStyle, CellStyle currencyStyle,
                                             CellStyle numberStyle, CellStyle percentStyle) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Sản phẩm", "Số lượng", "Doanh thu", "Tỷ lệ %"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        for (DealerPerformanceResponse.ProductPerformance product : report.getProductBreakdown()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getProductName());

            Cell unitsCell = row.createCell(1);
            unitsCell.setCellValue(product.getUnitsSold());
            unitsCell.setCellStyle(numberStyle);

            Cell revenueCell = row.createCell(2);
            revenueCell.setCellValue(product.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);

            Cell percentCell = row.createCell(3);
            percentCell.setCellValue(product.getPercentage().doubleValue() / 100);
            percentCell.setCellStyle(percentStyle);
        }
    }

// ============================================================
// COMMON HELPER METHODS
// ============================================================

    private void createSummaryRow(Sheet sheet, int rowNum, String label, Object value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);

        Cell valueCell = row.createCell(1);
        if (value instanceof BigDecimal) {
            valueCell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else {
            valueCell.setCellValue(value.toString());
        }

        if (style != null) {
            valueCell.setCellStyle(style);
        }
    }

    private void createNumberCell(Row row, int columnIndex, Integer value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
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
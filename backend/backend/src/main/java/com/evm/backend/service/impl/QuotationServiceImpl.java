package com.evm.backend.service.impl;

import com.evm.backend.dto.request.QuotationRequest;
import com.evm.backend.dto.response.QuotationResponse;
import com.evm.backend.dto.response.SalesOrderResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.QuotationService;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final PromotionRepository promotionRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final QuotationPromotionRepository quotationPromotionRepository;

    // Thêm các constant vào class QuotationServiceImpl
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
//    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10"); // 10%

    @Override
    @Transactional
    public QuotationResponse createQuotation(QuotationRequest request) {
        log.info("Creating quotation for customer: {}", request.getCustomerId());

        // Validate entities
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        User salesPerson = null;
        if (request.getSalesPersonId() != null) {
            salesPerson = userRepository.findById(request.getSalesPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales person not found"));
        }

        // Generate quotation number
        String quotationNumber = generateQuotationNumber();

        // Calculate prices
        BigDecimal basePrice = request.getBasePrice();
        BigDecimal vat = basePrice.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal registrationFee = request.getRegistrationFee() != null ?
                request.getRegistrationFee() : BigDecimal.ZERO;

        // Calculate discount from promotions
        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            totalDiscount = calculateTotalDiscount(basePrice, request.getPromotionIds());
        }

        // Calculate total
        BigDecimal totalPrice = basePrice
                .add(vat)
                .add(registrationFee)
                .subtract(totalDiscount)
                .setScale(2, RoundingMode.HALF_UP);

        // Create quotation
        Quotation quotation = Quotation.builder()
                .quotationNumber(quotationNumber)
                .quotationDate(request.getQuotationDate() != null ?
                        request.getQuotationDate() : LocalDate.now())
                .validUntil(request.getValidUntil() != null ?
                        request.getValidUntil() : LocalDate.now().plusDays(30))
                .basePrice(basePrice)
                .vat(vat)
                .registrationFee(registrationFee)
                .discountAmount(totalDiscount)
                .totalPrice(totalPrice)
                .status("DRAFT")
                .notes(request.getNotes())
                .termsAndConditions(request.getTermsAndConditions())
                .product(product)
                .customer(customer)
                .salesPerson(salesPerson)
                .dealer(dealer)
                .build();

        Quotation saved = quotationRepository.save(quotation);

        // Add promotions
        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            addPromotionsToQuotation(saved, request.getPromotionIds(), basePrice);
        }

        log.info("Quotation created: {}", quotationNumber);
        return convertToResponse(saved);
    }

    @Override
    public QuotationResponse getQuotationById(Long id) {
        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        return convertToResponse(quotation);
    }

    @Override
    public QuotationResponse getQuotationByNumber(String quotationNumber) {
        Quotation quotation = quotationRepository.findByQuotationNumber(quotationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        return convertToResponse(quotation);
    }

    @Override
    public Page<QuotationResponse> getAllQuotations(Pageable pageable) {
        return quotationRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotation(Long id, QuotationRequest request) {
        log.info("Updating quotation: {}", id);

        // 1. Load quotation (without promotions details for now)
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!"DRAFT".equals(quotation.getStatus())) {
            throw new BadRequestException("Chỉ có thể sửa báo giá ở trạng thái DRAFT");
        }

        // 2. Update basic fields
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        quotation.setProduct(product);
        quotation.setCustomer(customer);

        if (request.getSalesPersonId() != null) {
            User salesPerson = userRepository.findById(request.getSalesPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales person not found"));
            quotation.setSalesPerson(salesPerson);
        }

        // 3. Update dates
        if (request.getQuotationDate() != null) {
            quotation.setQuotationDate(request.getQuotationDate());
        }
        if (request.getValidUntil() != null) {
            quotation.setValidUntil(request.getValidUntil());
        }

        quotation.setNotes(request.getNotes());
        quotation.setTermsAndConditions(request.getTermsAndConditions());

        // 4. ✅ DELETE old promotions using repository (more reliable)
        log.info("Deleting old promotions for quotation {}", id);
        quotationPromotionRepository.deleteByQuotationId(id);
        quotationPromotionRepository.flush();

        // 5. Calculate prices
        BigDecimal basePrice = request.getBasePrice();
        BigDecimal vat = basePrice.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal registrationFee = request.getRegistrationFee() != null ?
                request.getRegistrationFee() : BigDecimal.ZERO;

        // 6. ✅ CREATE and SAVE new promotions one by one
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<QuotationPromotion> newPromotions = new ArrayList<>();

        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            log.info("Adding {} promotions to quotation {}", request.getPromotionIds().size(), id);

            for (Long promotionId : request.getPromotionIds()) {
                log.debug("Processing promotion ID: {}", promotionId);

                Promotion promotion = promotionRepository.findById(promotionId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Promotion not found: " + promotionId));

                BigDecimal appliedAmount = calculatePromotionDiscount(basePrice, promotion);
                totalDiscount = totalDiscount.add(appliedAmount);

                QuotationPromotion qp = QuotationPromotion.builder()
                        .quotation(quotation)
                        .promotion(promotion)
                        .appliedAmount(appliedAmount)
                        .build();

                newPromotions.add(qp);

                log.debug("Created promotion: {} - {} with applied amount: {}",
                        promotionId, promotion.getPromotionName(), appliedAmount);
            }
        }

        // 7. Calculate total price
        BigDecimal totalPrice = basePrice
                .add(vat)
                .add(registrationFee)
                .subtract(totalDiscount)
                .setScale(2, RoundingMode.HALF_UP);

        // 8. Update quotation prices
        quotation.setBasePrice(basePrice);
        quotation.setVat(vat);
        quotation.setRegistrationFee(registrationFee);
        quotation.setDiscountAmount(totalDiscount);
        quotation.setTotalPrice(totalPrice);

        // 9. Save quotation first
        Quotation updated = quotationRepository.save(quotation);
        log.info("Quotation {} saved", id);

        // 10. ✅ Save all promotions at once
        if (!newPromotions.isEmpty()) {
            List<QuotationPromotion> savedPromotions = quotationPromotionRepository.saveAll(newPromotions);
            log.info("Saved {} promotions for quotation {}", savedPromotions.size(), id);
        }

        // 11. ✅ RELOAD quotation with promotions
        updated = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found after update"));

        log.info("Quotation {} updated successfully with {} promotions",
                id, updated.getQuotationPromotions().size());

        return convertToResponse(updated);
    }

    public void deleteQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        // Validation 1: Chỉ cho phép xóa quotation có status = DRAFT
        if (!"DRAFT".equals(quotation.getStatus())) {
            throw new BadRequestException(
                    "Cannot delete quotation. Only DRAFT quotations can be deleted. Current status: " + quotation.getStatus()
            );
        }

        // Validation 2: Không cho xóa nếu đã được convert thành sales order
        if (quotation.getSalesOrder() != null) {
            throw new BadRequestException(
                    "Cannot delete quotation that has been converted to a sales order (Order ID: " +
                            quotation.getSalesOrder().getId() + ")"
            );
        }

        // Clear các promotion relationships trước
        if (quotation.getQuotationPromotions() != null) {
            quotation.getQuotationPromotions().clear();
            quotationRepository.flush();
        }

        // Xóa quotation
        quotationRepository.delete(quotation);
        log.info("Successfully deleted quotation: {}", quotation.getQuotationNumber());
    }

    @Override
    @Transactional
    public QuotationResponse sendQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        quotation.setStatus("SENT");
        Quotation updated = quotationRepository.save(quotation);

        // TODO: Send email to customer

        log.info("Quotation sent: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public QuotationResponse acceptQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!"SENT".equals(quotation.getStatus())) {
            throw new BadRequestException("Chỉ có thể chấp nhận báo giá đã được gửi");
        }

        if (LocalDate.now().isAfter(quotation.getValidUntil())) {
            throw new BadRequestException("Báo giá đã hết hạn");
        }

        quotation.setStatus("ACCEPTED");
        Quotation updated = quotationRepository.save(quotation);

        log.info("Quotation accepted: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public QuotationResponse rejectQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        quotation.setStatus("REJECTED");
        Quotation updated = quotationRepository.save(quotation);

        log.info("Quotation rejected: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public SalesOrderResponse convertToOrder(Long id) {
        // Find quotation with all details
        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        // Validation
        if (!"ACCEPTED".equals(quotation.getStatus())) {
            throw new BadRequestException("Chỉ có thể chuyển báo giá đã được chấp nhận thành đơn hàng");
        }

        if (quotation.getSalesOrder() != null) {
            throw new BadRequestException("Báo giá này đã được chuyển thành đơn hàng");
        }

        // Check if quotation is expired
        if (quotation.getValidUntil() != null &&
                quotation.getValidUntil().isBefore(LocalDate.now())) {
            throw new BadRequestException("Báo giá đã hết hiệu lực");
        }

        // Create sales order
        SalesOrder order = SalesOrder.builder()
                .orderDate(LocalDate.now())
                .basePrice(quotation.getBasePrice())
                .vat(quotation.getVat())
                .registrationFee(quotation.getRegistrationFee())
                .discountAmount(quotation.getDiscountAmount())
                .totalPrice(quotation.getTotalPrice())
                .status("PENDING") // Đang chờ gán xe
                .vehicle(null) // Chưa có xe cụ thể
                .customer(quotation.getCustomer())
                .salesPerson(quotation.getSalesPerson())
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(order);
        log.info("Created order {} from quotation {}", savedOrder.getId(), quotation.getId());

        // Copy promotions from quotation to order
        if (quotation.getQuotationPromotions() != null &&
                !quotation.getQuotationPromotions().isEmpty()) {

            Set<OrderPromotions> orderPromotions = new HashSet<>();
            for (QuotationPromotion qp : quotation.getQuotationPromotions()) {
                OrderPromotions op = OrderPromotions.builder()
                        .order(savedOrder)
                        .promotion(qp.getPromotion())
//                        .appliedAmount(qp.getAppliedAmount())
                        .build();
                orderPromotions.add(op);
            }

            if (!orderPromotions.isEmpty()) {
                savedOrder.setOrderPromotions(orderPromotions);
                savedOrder = salesOrderRepository.save(savedOrder);
                log.info("Copied {} promotions to order {}",
                        orderPromotions.size(), savedOrder.getId());
            }
        }

        // Update quotation status and link to order
        quotation.setStatus("CONVERTED");
        quotation.setSalesOrder(savedOrder);
        quotationRepository.save(quotation);
        log.info("Updated quotation {} status to CONVERTED", quotation.getId());

        // Build and return response
        return buildSalesOrderResponse(savedOrder, quotation);
    }

    /**
     * Build SalesOrderResponse from SalesOrder and Quotation
     */
    private SalesOrderResponse buildSalesOrderResponse(SalesOrder order, Quotation quotation) {
        // Calculate payment info
        BigDecimal paidAmount = BigDecimal.ZERO;
        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            paidAmount = order.getPayments().stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()))
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal remainingAmount = order.getTotalPrice().subtract(paidAmount);
        boolean isPaid = remainingAmount.compareTo(BigDecimal.ZERO) <= 0;

        // Calculate days from order
        int daysFromOrder = (int) ChronoUnit.DAYS.between(order.getOrderDate(), LocalDate.now());

        // Build message
        String message = "Đơn hàng đã được tạo thành công từ báo giá " + quotation.getQuotationNumber() +
                ". Vui lòng gán xe cho đơn hàng để tiếp tục.";

        return SalesOrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                // Vehicle info - null vì chưa gán xe
                .vehicleId(null)
                .vehicleModel(null)
                .vehicleBrand(null)
                .vehicleVin(null)
                // Customer info
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhoneNumber() : null)
                // Sales person info
                .salesPersonId(order.getSalesPerson() != null ? order.getSalesPerson().getId() : null)
                .salesPersonName(order.getSalesPerson() != null ? order.getSalesPerson().getFullName() : null)
                // Quotation reference
                .quotationId(quotation.getId())
                .quotationNumber(quotation.getQuotationNumber())
                // Calculated fields
                .daysFromOrder(daysFromOrder)
                .isPaid(isPaid)
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                // Message
                .message(message)
                .build();
    }

    @Override
    public byte[] exportQuotationToPdf(Long id) {
        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);

            // Tạo fonts hỗ trợ tiếng Việt với itext-asian
            BaseFont bfBold = BaseFont.createFont("c:/windows/fonts/arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bfNormal = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font fontTitle = new Font(bfBold, 18, Font.BOLD);
            Font fontHeader = new Font(bfBold, 12, Font.BOLD);
            Font fontNormal = new Font(bfNormal, 10, Font.NORMAL);
            Font fontSmall = new Font(bfNormal, 8, Font.NORMAL);

            document.open();

            // 1. Header - Company Info
            addCompanyHeader(document, fontHeader, fontNormal, fontSmall);

            // 2. Title
            addTitle(document, quotation, fontTitle, fontNormal);

            // 3. Quotation Info
            addQuotationInfo(document, quotation, fontHeader, fontNormal);

            // 4. Customer Info
            addCustomerInfo(document, quotation, fontHeader, fontNormal);

            // 5. Product Details Table
            addProductTable(document, quotation, fontHeader, fontNormal);

            // 6. Price Summary
            addPriceSummary(document, quotation, fontHeader, fontNormal);

            // 7. Terms and Conditions
            addTermsAndConditions(document, quotation, fontHeader, fontNormal);

            // 8. Footer
            addFooter(document, quotation, fontHeader, fontNormal, fontSmall);

            document.close();

            log.info("PDF generated successfully for quotation: {}", quotation.getQuotationNumber());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for quotation: {}", quotation.getQuotationNumber(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

// ============================================================
// BƯỚC 5: Thêm các helper methods vào cuối class
// ============================================================

    private void addCompanyHeader(Document document, Font fontHeader, Font fontNormal, Font fontSmall)
            throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(20);

        // Left side - Company info
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        Paragraph company = new Paragraph();
        company.add(new Phrase("CÔNG TY XE ĐIỆN EVM\n", fontHeader));
        company.add(new Phrase("Electric Vehicle Management System\n", fontNormal));
        company.add(new Phrase("Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM\n", fontSmall));
        company.add(new Phrase("Hotline: 1900-xxxx | Email: sales@evm.vn", fontSmall));
        leftCell.addElement(company);

        // Right side
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph info = new Paragraph();
        info.add(new Phrase("Mã số thuế: 0123456789\n", fontSmall));
        info.add(new Phrase("Website: www.evm.vn\n", fontSmall));
        rightCell.addElement(info);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Separator
        LineSeparator line = new LineSeparator(1, 100, BaseColor.GRAY, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));
        document.add(new Paragraph(" "));
    }

    private void addTitle(Document document, Quotation quotation, Font fontTitle, Font fontNormal)
            throws DocumentException {
        Paragraph title = new Paragraph();
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        title.add(new Chunk("BÁO GIÁ XE ĐIỆN", fontTitle));
        document.add(title);

        Paragraph subtitle = new Paragraph();
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        subtitle.add(new Phrase("(Electric Vehicle Quotation)", fontNormal));
        document.add(subtitle);
    }

    private void addQuotationInfo(Document document, Quotation quotation, Font fontHeader, Font fontNormal)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        table.setWidths(new int[]{25, 25, 25, 25});

        addInfoCell(table, "Số báo giá:", quotation.getQuotationNumber(), fontHeader, fontNormal);
        addInfoCell(table, "Ngày báo giá:", quotation.getQuotationDate().format(DATE_FORMATTER), fontHeader, fontNormal);
        addInfoCell(table, "Có hiệu lực đến:", quotation.getValidUntil().format(DATE_FORMATTER), fontHeader, fontNormal);
        addInfoCell(table, "Trạng thái:", getStatusText(quotation.getStatus()), fontHeader, fontNormal);

        document.add(table);
    }

    private void addCustomerInfo(Document document, Quotation quotation, Font fontHeader, Font fontNormal)
            throws DocumentException {
        Paragraph header = new Paragraph("THÔNG TIN KHÁCH HÀNG", fontHeader);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        table.setWidths(new int[]{30, 70});

        if (quotation.getCustomer() != null) {
            addDetailRow(table, "Tên khách hàng:", quotation.getCustomer().getFullName(), fontHeader, fontNormal);
            addDetailRow(table, "Số điện thoại:", quotation.getCustomer().getPhoneNumber(), fontHeader, fontNormal);
            if (quotation.getCustomer().getEmail() != null) {
                addDetailRow(table, "Email:", quotation.getCustomer().getEmail(), fontHeader, fontNormal);
            }
            if (quotation.getCustomer().getAddress() != null) {
                addDetailRow(table, "Địa chỉ:", quotation.getCustomer().getAddress(), fontHeader, fontNormal);
            }
        }

        if (quotation.getSalesPerson() != null) {
            addDetailRow(table, "Nhân viên tư vấn:", quotation.getSalesPerson().getFullName(), fontHeader, fontNormal);
        }

        if (quotation.getDealer() != null) {
            addDetailRow(table, "Đại lý:", quotation.getDealer().getDealerName(), fontHeader, fontNormal);
        }

        document.add(table);
    }

    private void addProductTable(Document document, Quotation quotation, Font fontHeader, Font fontNormal)
            throws DocumentException {
        Paragraph header = new Paragraph("CHI TIẾT SẢN PHẨM", fontHeader);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        table.setWidths(new int[]{40, 20, 20, 20});

        // Header
        addTableHeader(table, "Sản phẩm", fontHeader);
        addTableHeader(table, "Phiên bản", fontHeader);
        addTableHeader(table, "Số lượng", fontHeader);
        addTableHeader(table, "Đơn giá", fontHeader);

        // Product row
        if (quotation.getProduct() != null) {
            addTableCell(table, quotation.getProduct().getProductName(), Element.ALIGN_LEFT, fontNormal);
            addTableCell(table, quotation.getProduct().getVersion(), Element.ALIGN_CENTER, fontNormal);
            addTableCell(table, "1", Element.ALIGN_CENTER, fontNormal);
            addTableCell(table, formatCurrency(quotation.getBasePrice()), Element.ALIGN_RIGHT, fontNormal);
        }

        document.add(table);
    }

    private void addPriceSummary(Document document, Quotation quotation, Font fontHeader, Font fontNormal)
            throws DocumentException {
        Paragraph header = new Paragraph("TỔNG KẾT GIÁ", fontHeader);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        table.setWidths(new int[]{70, 30});

        // Base price
        addSummaryRow(table, "Giá niêm yết:", formatCurrency(quotation.getBasePrice()), false, fontHeader, fontNormal);

        // VAT
        addSummaryRow(table, "VAT (10%):", formatCurrency(quotation.getVat()), false, fontHeader, fontNormal);

        // Registration fee
        if (quotation.getRegistrationFee() != null && quotation.getRegistrationFee().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(table, "Phí trước bạ:", formatCurrency(quotation.getRegistrationFee()), false, fontHeader, fontNormal);
        }

        // Promotions
        if (quotation.getQuotationPromotions() != null && !quotation.getQuotationPromotions().isEmpty()) {
            for (QuotationPromotion qp : quotation.getQuotationPromotions()) {
                String promotionLabel = "  - " + qp.getPromotion().getPromotionName() + ":";
                addSummaryRow(table, promotionLabel, "-" + formatCurrency(qp.getAppliedAmount()), false, fontHeader, fontNormal);
            }
        }

        // Total discount
        if (quotation.getDiscountAmount() != null && quotation.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(table, "Tổng giảm giá:", "-" + formatCurrency(quotation.getDiscountAmount()), false, fontHeader, fontNormal);
        }

        // Separator
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setColspan(2);
        separatorCell.setBorder(Rectangle.TOP);
        separatorCell.setPaddingTop(5);
        separatorCell.setPaddingBottom(5);
        table.addCell(separatorCell);

        // Total price
        addSummaryRow(table, "TỔNG CỘNG:", formatCurrency(quotation.getTotalPrice()), true, fontHeader, fontNormal);

        document.add(table);
    }

    private void addTermsAndConditions(Document document, Quotation quotation, Font fontHeader, Font fontNormal)
            throws DocumentException {
        if (quotation.getTermsAndConditions() != null && !quotation.getTermsAndConditions().isEmpty()) {
            Paragraph header = new Paragraph("ĐIỀU KHOẢN & ĐIỀU KIỆN", fontHeader);
            header.setSpacingBefore(15);
            header.setSpacingAfter(10);
            document.add(header);

            Paragraph terms = new Paragraph(quotation.getTermsAndConditions(), fontNormal);
            terms.setAlignment(Element.ALIGN_JUSTIFIED);
            terms.setSpacingAfter(15);
            document.add(terms);
        }

        if (quotation.getNotes() != null && !quotation.getNotes().isEmpty()) {
            Paragraph header = new Paragraph("GHI CHÚ", fontHeader);
            header.setSpacingBefore(10);
            header.setSpacingAfter(10);
            document.add(header);

            Paragraph notes = new Paragraph(quotation.getNotes(), fontNormal);
            notes.setAlignment(Element.ALIGN_JUSTIFIED);
            notes.setSpacingAfter(15);
            document.add(notes);
        }
    }

    private void addFooter(Document document, Quotation quotation, Font fontHeader, Font fontNormal, Font fontSmall)
            throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(20);

        // Customer signature
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBorder(Rectangle.NO_BORDER);
        customerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph customer = new Paragraph();
        customer.add(new Phrase("KHÁCH HÀNG\n", fontHeader));
        customer.add(new Phrase("(Ký và ghi rõ họ tên)\n\n\n\n\n", fontSmall));
        customerCell.addElement(customer);

        // Sales person signature
        PdfPCell salesCell = new PdfPCell();
        salesCell.setBorder(Rectangle.NO_BORDER);
        salesCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph sales = new Paragraph();
        sales.add(new Phrase("NHÂN VIÊN TƯ VẤN\n", fontHeader));
        sales.add(new Phrase("(Ký và ghi rõ họ tên)\n\n\n\n", fontSmall));
        if (quotation.getSalesPerson() != null) {
            sales.add(new Phrase(quotation.getSalesPerson().getFullName(), fontNormal));
        }
        salesCell.addElement(sales);

        signatureTable.addCell(customerCell);
        signatureTable.addCell(salesCell);

        document.add(signatureTable);
    }

// ============================================================
// Utility helper methods
// ============================================================

    private void addInfoCell(PdfPTable table, String label, String value, Font fontHeader, Font fontNormal) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, fontHeader));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, fontNormal));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addDetailRow(PdfPTable table, String label, String value, Font fontHeader, Font fontNormal) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, fontHeader));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", fontNormal));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String text, Font fontHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, fontHeader));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, int alignment, Font fontNormal) {
        PdfPCell cell = new PdfPCell(new Phrase(text, fontNormal));
        cell.setPadding(8);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean isBold, Font fontHeader, Font fontNormal) {
        Font labelFont = isBold ? fontHeader : fontNormal;
        Font valueFont = isBold ? fontHeader : fontNormal;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return CURRENCY_FORMATTER.format(amount);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "DRAFT": return "Nháp";
            case "SENT": return "Đã gửi";
            case "ACCEPTED": return "Đã chấp nhận";
            case "REJECTED": return "Đã từ chối";
            case "EXPIRED": return "Hết hạn";
            case "CONVERTED": return "Đã chuyển đơn";
            default: return status;
        }
    }

    @Override
    public List<QuotationResponse> getQuotationsByCustomer(Long customerId) {
        return quotationRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationResponse> getQuotationsBySalesPerson(Long salesPersonId) {
        return quotationRepository.findBySalesPersonId(salesPersonId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotationResponse> getExpiredQuotations() {
        return quotationRepository.findExpiredQuotations(LocalDate.now())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void autoExpireQuotations() {
        List<Quotation> expiredQuotations = quotationRepository.findExpiredQuotations(LocalDate.now());

        for (Quotation quotation : expiredQuotations) {
            quotation.setStatus("EXPIRED");
        }

        quotationRepository.saveAll(expiredQuotations);
        log.info("Auto-expired {} quotations", expiredQuotations.size());
    }

    @Override
    @Transactional
    public QuotationResponse recalculateQuotation(Long id) {
        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        // Recalculate from scratch
        BigDecimal basePrice = quotation.getBasePrice();
        BigDecimal vat = basePrice.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDiscount = quotation.getQuotationPromotions().stream()
                .map(QuotationPromotion::getAppliedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrice = basePrice
                .add(vat)
                .add(quotation.getRegistrationFee())
                .subtract(totalDiscount);

        quotation.setVat(vat);
        quotation.setDiscountAmount(totalDiscount);
        quotation.setTotalPrice(totalPrice);

        Quotation updated = quotationRepository.save(quotation);
        return convertToResponse(updated);
    }

    // Helper methods

    private String generateQuotationNumber() {
        String prefix = "QT-" + LocalDate.now().getYear() + "-";
        Long count = quotationRepository.count() + 1;
        return prefix + String.format("%05d", count);
    }

    private BigDecimal calculateTotalDiscount(BigDecimal basePrice, java.util.Set<Long> promotionIds) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (Long promotionId : promotionIds) {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

            BigDecimal discount = calculatePromotionDiscount(basePrice, promotion);
            totalDiscount = totalDiscount.add(discount);
        }

        return totalDiscount;
    }

    private BigDecimal calculatePromotionDiscount(BigDecimal basePrice, Promotion promotion) {
        if ("PERCENTAGE".equals(promotion.getDiscountType())) {
            return basePrice.multiply(promotion.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if ("FIXED".equals(promotion.getDiscountType())) {
            return promotion.getDiscountValue();
        }
        return BigDecimal.ZERO;
    }

    private void addPromotionsToQuotation(Quotation quotation, java.util.Set<Long> promotionIds,
                                          BigDecimal basePrice) {
        for (Long promotionId : promotionIds) {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

            BigDecimal appliedAmount = calculatePromotionDiscount(basePrice, promotion);

            QuotationPromotion qp = QuotationPromotion.builder()
                    .quotation(quotation)
                    .promotion(promotion)
                    .appliedAmount(appliedAmount)
                    .build();

            quotation.getQuotationPromotions().add(qp);
        }
    }

    private QuotationResponse convertToResponse(Quotation q) {
        LocalDate today = LocalDate.now();
        LocalDate validUntilDate = q.getValidUntil();

        boolean isExpired = (validUntilDate == null) || validUntilDate.isBefore(today);
        int daysUntilExpiry = (validUntilDate != null)
                ? (int) ChronoUnit.DAYS.between(today, validUntilDate)
                : 0;

        // ✅ Fix: Dùng getSalesOrder() thay vì getSalesOrderId()
        boolean canConvert = "ACCEPTED".equals(q.getStatus())
                && !isExpired
                && q.getSalesOrder() == null; // ✅ Đổi từ getSalesOrderId()

        QuotationResponse.QuotationResponseBuilder builder = QuotationResponse.builder()
                .id(q.getId())
                .quotationNumber(q.getQuotationNumber())
                .quotationDate(q.getQuotationDate())
                .validUntil(q.getValidUntil())
                .status(q.getStatus())
                .basePrice(q.getBasePrice())
                .vat(q.getVat())
                .registrationFee(q.getRegistrationFee())
                .discountAmount(q.getDiscountAmount())
                .totalPrice(q.getTotalPrice())
                .notes(q.getNotes())
                .termsAndConditions(q.getTermsAndConditions())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .isExpired(isExpired)
                .daysUntilExpiry(daysUntilExpiry)
                .canConvertToOrder(canConvert);

        // Product
        if (q.getProduct() != null) {
            builder.productId(q.getProduct().getId())
                    .productName(q.getProduct().getProductName())
                    .productVersion(q.getProduct().getVersion())
                    .productImageUrl(q.getProduct().getImageUrl());
        }

        // Customer
        if (q.getCustomer() != null) {
            builder.customerId(q.getCustomer().getId())
                    .customerName(q.getCustomer().getFullName())
                    .customerEmail(q.getCustomer().getEmail())
                    .customerPhone(q.getCustomer().getPhoneNumber());
        }

        // Sales person
        if (q.getSalesPerson() != null) {
            builder.salesPersonId(q.getSalesPerson().getId())
                    .salesPersonName(q.getSalesPerson().getFullName());
        }

        // Dealer
        if (q.getDealer() != null) {
            builder.dealerId(q.getDealer().getId())
                    .dealerName(q.getDealer().getDealerName());
        }

        // Promotions
        if (q.getQuotationPromotions() != null && !q.getQuotationPromotions().isEmpty()) {
            List<QuotationResponse.PromotionSummary> promotions =
                    q.getQuotationPromotions().stream()
                            .map(qp -> QuotationResponse.PromotionSummary.builder()
                                    .promotionId(qp.getPromotion().getId())
                                    .promotionName(qp.getPromotion().getPromotionName())
                                    .discountType(qp.getPromotion().getDiscountType())
                                    .discountValue(qp.getPromotion().getDiscountValue())
                                    .appliedAmount(qp.getAppliedAmount())
                                    .build())
                            .collect(Collectors.toList());
            builder.promotions(promotions);
        }

        return builder.build();
    }
}
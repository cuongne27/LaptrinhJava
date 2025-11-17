package com.evm.backend.service.impl;

import com.evm.backend.dto.request.QuotationRequest;
import com.evm.backend.dto.response.QuotationResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!"DRAFT".equals(quotation.getStatus())) {
            throw new BadRequestException("Chỉ có thể sửa báo giá ở trạng thái DRAFT");
        }

        // Update fields
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

        // Recalculate prices
        BigDecimal basePrice = request.getBasePrice();
        BigDecimal vat = basePrice.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal registrationFee = request.getRegistrationFee() != null ?
                request.getRegistrationFee() : BigDecimal.ZERO;

        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            totalDiscount = calculateTotalDiscount(basePrice, request.getPromotionIds());
        }

        BigDecimal totalPrice = basePrice.add(vat).add(registrationFee).subtract(totalDiscount);

        quotation.setBasePrice(basePrice);
        quotation.setVat(vat);
        quotation.setRegistrationFee(registrationFee);
        quotation.setDiscountAmount(totalDiscount);
        quotation.setTotalPrice(totalPrice);
        quotation.setNotes(request.getNotes());
        quotation.setTermsAndConditions(request.getTermsAndConditions());

        if (request.getValidUntil() != null) {
            quotation.setValidUntil(request.getValidUntil());
        }

        Quotation updated = quotationRepository.save(quotation);
        log.info("Quotation updated: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!"DRAFT".equals(quotation.getStatus())) {
            throw new BadRequestException("Chỉ có thể xóa báo giá ở trạng thái DRAFT");
        }

        quotationRepository.delete(quotation);
        log.info("Quotation deleted: {}", id);
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
    public QuotationResponse convertToOrder(Long id) {
        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!"ACCEPTED".equals(quotation.getStatus())) {
            throw new BadRequestException("Chỉ có thể chuyển báo giá đã được chấp nhận");
        }

//        if (quotation.getSalesOrder() != null) {
//            throw new BadRequestException("Báo giá đã được chuyển thành đơn hàng");
//        }

        // Create sales order
        SalesOrder order = SalesOrder.builder()
                .orderDate(LocalDate.now())
                .basePrice(quotation.getBasePrice())
                .vat(quotation.getVat())
                .registrationFee(quotation.getRegistrationFee())
                .discountAmount(quotation.getDiscountAmount())
                .totalPrice(quotation.getTotalPrice())
                .status("PENDING")
                .vehicle(null) // Chưa có VIN
                .customer(quotation.getCustomer())
                .salesPerson(quotation.getSalesPerson())
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(order);

        // Update quotation status
        quotation.setStatus("CONVERTED");
//        quotation.setSalesOrder(savedOrder);
        quotationRepository.save(quotation);

        log.info("Quotation converted to order: {} -> {}", id, savedOrder.getId());
        return convertToResponse(quotation);
    }

    @Override
    public byte[] exportQuotationToPdf(Long id) {
        // TODO: Implement PDF export using iText or similar library
        throw new UnsupportedOperationException("PDF export not implemented yet");
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
        boolean isExpired = q.getValidUntil().isBefore(today);
        int daysUntilExpiry = (int) ChronoUnit.DAYS.between(today, q.getValidUntil());
//        boolean canConvert = "ACCEPTED".equals(q.getStatus()) && !isExpired && q.getSalesOrder() == null;

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
                .daysUntilExpiry(daysUntilExpiry);
//                .canConvertToOrder(canConvert);

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
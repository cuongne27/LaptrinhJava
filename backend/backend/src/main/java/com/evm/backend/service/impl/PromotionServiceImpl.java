package com.evm.backend.service.impl;

import com.evm.backend.dto.request.PromotionFilterRequest;
import com.evm.backend.dto.request.PromotionRequest;
import com.evm.backend.dto.response.PromotionDetailResponse;
import com.evm.backend.dto.response.PromotionListResponse;
import com.evm.backend.entity.Promotion;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.PromotionRepository;
import com.evm.backend.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public Page<PromotionListResponse> getAllPromotions(PromotionFilterRequest filterRequest) {
        Pageable pageable = buildPageable(filterRequest);
        Page<Promotion> promotionsPage;

        if (filterRequest.getStatus() != null) {
            LocalDate currentDate = LocalDate.now();
            List<Promotion> promotions;
            switch (filterRequest.getStatus().toUpperCase()) {
                case "ACTIVE":
                    promotions = promotionRepository.findActivePromotions(currentDate);
                    break;
                case "UPCOMING":
                    promotions = promotionRepository.findUpcomingPromotions(currentDate);
                    break;
                case "EXPIRED":
                    promotions = promotionRepository.findExpiredPromotions(currentDate);
                    break;
                default:
                    promotionsPage = promotionRepository.findPromotionsWithFilters(
                            filterRequest.getSearchKeyword(), filterRequest.getDiscountType(),
                            filterRequest.getFromDate(), filterRequest.getToDate(), pageable);
                    return promotionsPage.map(this::convertToListResponse);
            }

            List<PromotionListResponse> responseList = promotions.stream()
                    .map(this::convertToListResponse)
                    .toList();

            return new org.springframework.data.domain.PageImpl<>(
                    responseList,
                    org.springframework.data.domain.Pageable.unpaged(), // Dùng Pageable.unpaged() vì dữ liệu đã được lấy hết
                    responseList.size() // Tổng số phần tử
            );

//            return org.springframework.data.domain.PageImpl.of(
//                    promotions.stream().map(this::convertToListResponse).collect(Collectors.toList()));
        } else {
            promotionsPage = promotionRepository.findPromotionsWithFilters(
                    filterRequest.getSearchKeyword(), filterRequest.getDiscountType(),
                    filterRequest.getFromDate(), filterRequest.getToDate(), pageable);
            return promotionsPage.map(this::convertToListResponse);
        }
    }

    @Override
    public List<PromotionListResponse> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now()).stream()
                .map(this::convertToListResponse).collect(Collectors.toList());
    }

    @Override
    public PromotionDetailResponse getPromotionById(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + promotionId));
        return convertToDetailResponse(promotion);
    }

    @Override
    public PromotionDetailResponse getPromotionByCode(String promotionCode) {
        Promotion promotion = promotionRepository.findByPromotionCode(promotionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + promotionCode));
        return convertToDetailResponse(promotion);
    }

    @Override
    @Transactional
    public PromotionDetailResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByPromotionCode(request.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists: " + request.getPromotionCode());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if ("PERCENTAGE".equals(request.getDiscountType()) &&
                request.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }

        Promotion promotion = Promotion.builder()
                .promotionCode(request.getPromotionCode())
                .promotionName(request.getPromotionName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .conditions(request.getConditions())
                .build();

        return convertToDetailResponse(promotionRepository.save(promotion));
    }

    @Override
    @Transactional
    public PromotionDetailResponse updatePromotion(Long promotionId, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + promotionId));

        if (!promotion.getPromotionCode().equals(request.getPromotionCode()) &&
                promotionRepository.existsByPromotionCode(request.getPromotionCode())) {
            throw new IllegalArgumentException("Promotion code already exists: " + request.getPromotionCode());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        promotion.setPromotionCode(request.getPromotionCode());
        promotion.setPromotionName(request.getPromotionName());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setConditions(request.getConditions());

        return convertToDetailResponse(promotionRepository.save(promotion));
    }

    @Override
    @Transactional
    public void deletePromotion(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new ResourceNotFoundException("Promotion not found: " + promotionId);
        }

        if (promotionRepository.isPromotionInUse(promotionId)) {
            throw new IllegalStateException("Cannot delete promotion that is being used in orders");
        }

        promotionRepository.deleteById(promotionId);
    }

    private PromotionListResponse convertToListResponse(Promotion promotion) {
        String status = calculateStatus(promotion.getStartDate(), promotion.getEndDate());
        Long daysRemaining = calculateDaysRemaining(promotion.getEndDate());
        Long totalUsages = promotionRepository.countUsagesByPromotionId(promotion.getId());

        return PromotionListResponse.builder()
                .id(promotion.getId())
                .promotionCode(promotion.getPromotionCode())
                .promotionName(promotion.getPromotionName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(status)
                .totalUsages(totalUsages)
                .daysRemaining(daysRemaining)
                .build();
    }

    private PromotionDetailResponse convertToDetailResponse(Promotion promotion) {
        String status = calculateStatus(promotion.getStartDate(), promotion.getEndDate());
        Long daysRemaining = calculateDaysRemaining(promotion.getEndDate());
        Integer progressPercentage = calculateProgressPercentage(promotion.getStartDate(), promotion.getEndDate());
        Long totalUsages = promotionRepository.countUsagesByPromotionId(promotion.getId());
        String discountDisplay = formatDiscountDisplay(promotion.getDiscountType(), promotion.getDiscountValue());

        return PromotionDetailResponse.builder()
                .id(promotion.getId())
                .promotionCode(promotion.getPromotionCode())
                .promotionName(promotion.getPromotionName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .conditions(promotion.getConditions())
                .status(status)
                .totalUsages(totalUsages)
                .daysRemaining(daysRemaining)
                .progressPercentage(progressPercentage)
                .discountDisplay(discountDisplay)
                .build();
    }

    private String calculateStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(startDate)) return "UPCOMING";
        else if (currentDate.isAfter(endDate)) return "EXPIRED";
        else return "ACTIVE";
    }

    private Long calculateDaysRemaining(LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.isAfter(endDate) ? 0L : ChronoUnit.DAYS.between(currentDate, endDate);
    }

    private Integer calculateProgressPercentage(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(startDate)) return 0;
        if (currentDate.isAfter(endDate)) return 100;

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, currentDate);
        return (int) ((elapsedDays * 100) / totalDays);
    }

    private String formatDiscountDisplay(String discountType, java.math.BigDecimal discountValue) {
        if ("PERCENTAGE".equals(discountType)) {
            return discountValue + "%";
        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(discountValue);
        }
    }

    private Pageable buildPageable(PromotionFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;
        if (size > 100) size = 100;

        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null) {
            switch (filterRequest.getSortBy()) {
                case "code_asc": sort = Sort.by(Sort.Direction.ASC, "promotionCode"); break;
                case "code_desc": sort = Sort.by(Sort.Direction.DESC, "promotionCode"); break;
                case "start_date_asc": sort = Sort.by(Sort.Direction.ASC, "startDate"); break;
                case "start_date_desc": sort = Sort.by(Sort.Direction.DESC, "startDate"); break;
                default: sort = Sort.by(Sort.Direction.DESC, "startDate");
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
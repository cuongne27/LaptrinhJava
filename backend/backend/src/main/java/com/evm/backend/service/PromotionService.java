package com.evm.backend.service;

import com.evm.backend.dto.request.PromotionFilterRequest;
import com.evm.backend.dto.request.PromotionRequest;
import com.evm.backend.dto.response.PromotionDetailResponse;
import com.evm.backend.dto.response.PromotionListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PromotionService {
    Page<PromotionListResponse> getAllPromotions(PromotionFilterRequest filterRequest);
    List<PromotionListResponse> getActivePromotions();
    PromotionDetailResponse getPromotionById(Long promotionId);
    PromotionDetailResponse getPromotionByCode(String promotionCode);
    PromotionDetailResponse createPromotion(PromotionRequest request);
    PromotionDetailResponse updatePromotion(Long promotionId, PromotionRequest request);
    void deletePromotion(Long promotionId);
}
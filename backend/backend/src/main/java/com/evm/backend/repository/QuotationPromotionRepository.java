package com.evm.backend.repository;

import com.evm.backend.entity.QuotationPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationPromotionRepository extends JpaRepository<QuotationPromotion, Long> {

    @Modifying
    @Query("DELETE FROM QuotationPromotion qp WHERE qp.quotation.id = :quotationId")
    void deleteByQuotationId(@Param("quotationId") Long quotationId);

    List<QuotationPromotion> findByQuotationId(Long quotationId);
}

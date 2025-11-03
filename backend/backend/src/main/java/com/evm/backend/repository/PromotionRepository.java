package com.evm.backend.repository;

import com.evm.backend.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByPromotionCode(String promotionCode);

    List<Promotion> findByDiscountType(String discountType);

//    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
//    List<Promotion> findActivePromotions();
//
//    @Query("SELECT p FROM Promotion p WHERE p.promotionCode = :code AND p.isActive = true AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
//    Optional<Promotion> findValidPromotionByCode(@Param("code") String code);
//
//    @Query("SELECT p FROM Promotion p WHERE p.endDate < :currentDate AND p.isActive = true")
//    List<Promotion> findExpiredPromotions(@Param("currentDate") LocalDate currentDate);

    boolean existsByPromotionCode(String promotionCode);
}
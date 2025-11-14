package com.evm.backend.repository;

import com.evm.backend.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Tìm promotion theo code
     */
    Optional<Promotion> findByPromotionCode(String promotionCode);

    /**
     * Check promotion code có tồn tại không
     */
    boolean existsByPromotionCode(String promotionCode);

    /**
     * Check promotion code có tồn tại không (exclude current promotion)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Promotion p " +
            "WHERE p.promotionCode = :promotionCode AND p.id <> :excludeId")
    boolean existsByPromotionCodeAndIdNot(
            @Param("promotionCode") String promotionCode,
            @Param("excludeId") Long excludeId
    );

    /**
     * Tìm active promotions (đang trong thời hạn)
     */
    @Query("SELECT p FROM Promotion p WHERE " +
            "p.startDate <= :currentDate AND " +
            "p.endDate >= :currentDate")
    List<Promotion> findActivePromotions(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm upcoming promotions (chưa bắt đầu)
     */
    @Query("SELECT p FROM Promotion p WHERE p.startDate > :currentDate")
    List<Promotion> findUpcomingPromotions(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm expired promotions (đã hết hạn)
     */
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :currentDate")
    List<Promotion> findExpiredPromotions(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm promotions với filter
     */
    @Query("SELECT p FROM Promotion p WHERE " +
            "(:searchKeyword IS NULL OR " +
            "LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) AND " +
            "(:discountType IS NULL OR p.discountType = :discountType) AND " +
            "(:fromDate IS NULL OR p.startDate >= :fromDate) AND " +
            "(:toDate IS NULL OR p.endDate <= :toDate)")
    Page<Promotion> findPromotionsWithFilters(
            @Param("searchKeyword") String searchKeyword,
            @Param("discountType") String discountType,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    /**
     * Đếm số lần promotion được sử dụng
     */
    @Query("SELECT COUNT(op) FROM OrderPromotions op WHERE op.promotion.id = :promotionId")
    Long countUsagesByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * Check promotion có đang được sử dụng không
     */
    @Query("SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END FROM OrderPromotions op " +
            "WHERE op.promotion.id = :promotionId")
    boolean isPromotionInUse(@Param("promotionId") Long promotionId);
}
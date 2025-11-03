package com.evm.backend.repository;

import com.evm.backend.entity.OrderPromotions;
import com.evm.backend.entity.Promotion;
import com.evm.backend.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderPromotionsRepository extends JpaRepository<OrderPromotions, Long> {
    List<OrderPromotions> findByOrder(SalesOrder order);

    List<OrderPromotions> findByOrderId(Long orderId);

    List<OrderPromotions> findByPromotion(Promotion promotion);

    List<OrderPromotions> findByPromotionId(Long promotionId);

//    @Query("SELECT SUM(op.appliedDiscount) FROM OrderPromotions op WHERE op.order.id = :orderId")
//    BigDecimal getTotalDiscountForOrder(@Param("orderId") Long orderId);
//
//    @Query("SELECT COUNT(op) FROM OrderPromotions op WHERE op.promotion.id = :promotionId")
//    long countUsageByPromotion(@Param("promotionId") Long promotionId);
}
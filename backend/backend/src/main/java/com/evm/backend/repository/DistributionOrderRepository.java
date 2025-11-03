package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import com.evm.backend.entity.DistributionOrder;
import com.evm.backend.entity.SellInRequest;
import com.evm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DistributionOrderRepository extends JpaRepository<DistributionOrder, Long> {
    List<DistributionOrder> findBySellInRequest(SellInRequest sellInRequest);

    List<DistributionOrder> findBySellInRequestId(Long requestId);

    List<DistributionOrder> findByDealerId(Long dealerId);

    List<DistributionOrder> findByBrand(Brand brand);

    List<DistributionOrder> findByBrandId(Integer brandId);

    List<DistributionOrder> findByStatus(String status);

    Optional<DistributionOrder> findByTrackingNumber(String trackingNumber);

    long countByDealerIdAndStatus(Long dealerId, String status);

    long countByBrandIdAndStatus(Integer brandId, String status);

//    @Query("SELECT do FROM DistributionOrder do WHERE do.dealerId = :dealerId AND do.status = :status")
//    List<DistributionOrder> findByDealerAndStatus(@Param("dealerId") Long dealerId,
//                                                  @Param("status") String status);
//
//    @Query("SELECT do FROM DistributionOrder do WHERE do.brand.id = :brandId AND do.orderDate BETWEEN :startDate AND :endDate")
//    List<DistributionOrder> findByBrandAndDateRange(@Param("brandId") Integer brandId,
//                                                    @Param("startDate") OffsetDateTime startDate,
//                                                    @Param("endDate") OffsetDateTime endDate);
//
//    @Query("SELECT do FROM DistributionOrder do WHERE do.status = 'SHIPPED' AND do.deliveryDate < CURRENT_TIMESTAMP")
//    List<DistributionOrder> findOverdueDeliveries();
//
//    @Query("SELECT SUM(do.totalAmount) FROM DistributionOrder do WHERE do.dealerId = :dealerId AND do.status = 'DELIVERED'")
//    BigDecimal getTotalPurchaseAmountByDealer(@Param("dealerId") Long dealerId);
//
//    @Query("SELECT SUM(do.totalQuantity) FROM DistributionOrder do WHERE do.brand.id = :brandId AND do.status = 'DELIVERED'")
//    Integer getTotalDistributedQuantityByBrand(@Param("brandId") Integer brandId);
}
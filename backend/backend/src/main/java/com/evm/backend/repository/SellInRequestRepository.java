package com.evm.backend.repository;

import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.SellInRequest;
import com.evm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SellInRequestRepository extends JpaRepository<SellInRequest, Long> {
    List<SellInRequest> findByDealer(Dealer dealer);

    List<SellInRequest> findByDealerId(Long dealerId);

    List<SellInRequest> findByApprover(User approver);

    List<SellInRequest> findByApproverId(Long approverId);

    List<SellInRequest> findByStatus(String status);

//    @Query("SELECT sir FROM SellInRequest sir WHERE sir.dealer.id = :dealerId AND sir.status = :status")
//    List<SellInRequest> findByDealerAndStatus(@Param("dealerId") Long dealerId,
//                                              @Param("status") String status);
//
//    @Query("SELECT sir FROM SellInRequest sir WHERE sir.requestDate BETWEEN :startDate AND :endDate")
//    List<SellInRequest> findByDateRange(@Param("startDate") LocalDate startDate,
//                                        @Param("endDate") LocalDate endDate);
//
//    @Query("SELECT sir FROM SellInRequest sir WHERE sir.status = 'PENDING' ORDER BY sir.requestDate ASC")
//    List<SellInRequest> findPendingRequests();
//
//    @Query("SELECT sir FROM SellInRequest sir WHERE sir.dealer.brand.id = :brandId AND sir.status = :status")
//    List<SellInRequest> findByBrandAndStatus(@Param("brandId") Integer brandId,
//                                             @Param("status") String status);
//
//    @Query("SELECT SUM(sir.totalAmount) FROM SellInRequest sir WHERE sir.dealer.id = :dealerId AND sir.status = 'APPROVED'")
//    BigDecimal getTotalApprovedAmountByDealer(@Param("dealerId") Long dealerId);

    long countByDealerIdAndStatus(Long dealerId, String status);
}
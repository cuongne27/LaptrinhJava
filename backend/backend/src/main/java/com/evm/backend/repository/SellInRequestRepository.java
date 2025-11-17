package com.evm.backend.repository;

import com.evm.backend.entity.SellInRequest;
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
public interface SellInRequestRepository extends JpaRepository<SellInRequest, Long> {

    Optional<SellInRequest> findByRequestNumber(String requestNumber);

    List<SellInRequest> findByDealerId(Long dealerId);

    List<SellInRequest> findByStatus(String status);

    @Query("SELECT s FROM SellInRequest s WHERE " +
            "(:dealerId IS NULL OR s.dealer.id = :dealerId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:fromDate IS NULL OR s.requestDate >= :fromDate) AND " +
            "(:toDate IS NULL OR s.requestDate <= :toDate)")
    Page<SellInRequest> findRequestsWithFilters(
            @Param("dealerId") Long dealerId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("SELECT s FROM SellInRequest s " +
            "LEFT JOIN FETCH s.dealer " +
            "LEFT JOIN FETCH s.sellInRequestDetails " +
            "WHERE s.id = :id")
    Optional<SellInRequest> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM SellInRequest s WHERE " +
            "s.status = 'PENDING' AND " +
            "s.requestDate < :cutoffDate")
    List<SellInRequest> findOldPendingRequests(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT COUNT(s) FROM SellInRequest s WHERE " +
            "s.dealer.id = :dealerId AND " +
            "s.status = :status")
    Long countByDealerAndStatus(
            @Param("dealerId") Long dealerId,
            @Param("status") String status
    );

    @Query("SELECT s FROM SellInRequest s WHERE " +
            "s.expectedDeliveryDate BETWEEN :startDate AND :endDate AND " +
            "s.status IN ('APPROVED', 'IN_TRANSIT')")
    List<SellInRequest> findUpcomingDeliveries(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
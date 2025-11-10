package com.evm.backend.repository;

import com.evm.backend.entity.DealerContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DealerContractRepository extends JpaRepository<DealerContract, Long> {

    /**
     * Tìm tất cả contracts của dealer
     */
    List<DealerContract> findByDealerId(Long dealerId);

    /**
     * Tìm tất cả contracts của brand
     */
    List<DealerContract> findByBrandId(Integer brandId);

    /**
     * Tìm contracts với filter
     */
    @Query("SELECT dc FROM DealerContract dc WHERE " +
            "(:brandId IS NULL OR dc.brand.id = :brandId) AND " +
            "(:dealerId IS NULL OR dc.dealer.id = :dealerId) AND " +
            "(:startDate IS NULL OR dc.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR dc.endDate <= :endDate)")
    Page<DealerContract> findContractsWithFilters(
            @Param("brandId") Integer brandId,
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Tìm active contracts của dealer (đang trong thời hạn)
     */
    @Query("SELECT dc FROM DealerContract dc WHERE " +
            "dc.dealer.id = :dealerId AND " +
            "dc.startDate <= :currentDate AND " +
            "dc.endDate >= :currentDate")
    List<DealerContract> findActiveContractsByDealer(
            @Param("dealerId") Long dealerId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Tìm expired contracts (đã hết hạn)
     */
    @Query("SELECT dc FROM DealerContract dc WHERE " +
            "dc.endDate < :currentDate")
    Page<DealerContract> findExpiredContracts(
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    /**
     * Tìm upcoming contracts (chưa bắt đầu)
     */
    @Query("SELECT dc FROM DealerContract dc WHERE " +
            "dc.startDate > :currentDate")
    Page<DealerContract> findUpcomingContracts(
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    /**
     * Check xem dealer có contract active không
     */
    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END FROM DealerContract dc WHERE " +
            "dc.dealer.id = :dealerId AND " +
            "dc.startDate <= :currentDate AND " +
            "dc.endDate >= :currentDate")
    boolean hasActiveContract(
            @Param("dealerId") Long dealerId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Check xem có contract nào overlap với khoảng thời gian không
     */
    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END FROM DealerContract dc WHERE " +
            "dc.dealer.id = :dealerId AND " +
            "dc.id <> :excludeId AND " +
            "((dc.startDate <= :endDate AND dc.endDate >= :startDate))")
    boolean hasOverlappingContract(
            @Param("dealerId") Long dealerId,
            @Param("excludeId") Long excludeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Check overlap cho new contract
     */
    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END FROM DealerContract dc WHERE " +
            "dc.dealer.id = :dealerId AND " +
            "((dc.startDate <= :endDate AND dc.endDate >= :startDate))")
    boolean hasOverlappingContractForNew(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
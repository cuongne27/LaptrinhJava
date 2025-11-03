package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.DealerContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealerContractRepository extends JpaRepository<DealerContract, Long> {
    List<DealerContract> findByBrand(Brand brand);

    List<DealerContract> findByBrandId(Integer brandId);

    List<DealerContract> findByDealer(Dealer dealer);

    List<DealerContract> findByDealerId(Long dealerId);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.dealer.id = :dealerId AND dc.endDate >= CURRENT_DATE ORDER BY dc.startDate DESC")
    List<DealerContract> findActiveContractsByDealer(@Param("dealerId") Long dealerId);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.brand.id = :brandId AND dc.dealer.id = :dealerId ORDER BY dc.startDate DESC")
    List<DealerContract> findByBrandAndDealer(@Param("brandId") Integer brandId,
                                              @Param("dealerId") Long dealerId);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.dealer.id = :dealerId AND :currentDate BETWEEN dc.startDate AND dc.endDate")
    Optional<DealerContract> findCurrentContractByDealer(@Param("dealerId") Long dealerId,
                                                         @Param("currentDate") LocalDate currentDate);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.endDate < :currentDate")
    List<DealerContract> findExpiredContracts(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.endDate BETWEEN :startDate AND :endDate")
    List<DealerContract> findExpiringContracts(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.brand.id = :brandId AND dc.endDate >= CURRENT_DATE ORDER BY dc.salesTarget DESC")
    List<DealerContract> findActiveContractsByBrand(@Param("brandId") Integer brandId);

    @Query("SELECT dc FROM DealerContract dc WHERE dc.startDate <= :date AND dc.endDate >= :date")
    List<DealerContract> findValidContractsOnDate(@Param("date") LocalDate date);

    long countByBrandId(Integer brandId);

    long countByDealerId(Long dealerId);

    @Query("SELECT COUNT(dc) FROM DealerContract dc WHERE dc.endDate >= CURRENT_DATE")
    long countActiveContracts();
}
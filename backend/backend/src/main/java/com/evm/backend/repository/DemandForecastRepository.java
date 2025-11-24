package com.evm.backend.repository;

import com.evm.backend.entity.DemandForecast;
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
public interface DemandForecastRepository extends JpaRepository<DemandForecast, Long> {

    @Query("SELECT df FROM DemandForecast df " +
            "LEFT JOIN FETCH df.product p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH df.createdBy " +
            "WHERE df.id = :id")
    Optional<DemandForecast> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT df FROM DemandForecast df " +
            "WHERE (:productId IS NULL OR df.product.id = :productId) " +
            "AND (:brandId IS NULL OR df.product.brand.id = :brandId) " +
            "AND (:forecastPeriod IS NULL OR df.forecastPeriod = :forecastPeriod) " +
            "AND (:fromDate IS NULL OR df.forecastDate >= :fromDate) " +
            "AND (:toDate IS NULL OR df.forecastDate <= :toDate) " +
            "AND (:status IS NULL OR df.status = :status)")
    Page<DemandForecast> findWithFilters(
            @Param("productId") Long productId,
            @Param("brandId") Long brandId,
            @Param("forecastPeriod") String forecastPeriod,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            Pageable pageable
    );

    List<DemandForecast> findByProductIdAndForecastDateBetween(
            Long productId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT df FROM DemandForecast df " +
            "WHERE df.product.id = :productId " +
            "AND df.forecastPeriod = :period " +
            "AND df.forecastDate = :forecastDate")
    Optional<DemandForecast> findByProductAndPeriodAndDate(
            @Param("productId") Long productId,
            @Param("period") String period,
            @Param("forecastDate") LocalDate forecastDate
    );

    @Query("SELECT df FROM DemandForecast df " +
            "WHERE df.actualDemand IS NOT NULL " +
            "AND df.product.id = :productId " +
            "ORDER BY df.forecastDate DESC")
    List<DemandForecast> findHistoricalForecastsWithActuals(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT AVG((1.0 - ABS(df.actualDemand - df.predictedDemand) / df.actualDemand) * 100) " +
            "FROM DemandForecast df " +
            "WHERE df.actualDemand IS NOT NULL " +
            "AND df.product.id = :productId " +
            "AND df.forecastDate >= :fromDate")
    Double calculateAverageAccuracy(@Param("productId") Long productId, @Param("fromDate") LocalDate fromDate);
}
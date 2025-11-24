package com.evm.backend.repository;

import com.evm.backend.entity.Quotation;
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
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    List<Quotation> findByCustomerId(Long customerId);

    List<Quotation> findBySalesPersonId(Long salesPersonId);

    List<Quotation> findByDealerId(Long dealerId);

    List<Quotation> findByStatus(String status);

    @Query("SELECT q FROM Quotation q WHERE " +
            "(:customerId IS NULL OR q.customer.id = :customerId) AND " +
            "(:salesPersonId IS NULL OR q.salesPerson.id = :salesPersonId) AND " +
            "(:dealerId IS NULL OR q.dealer.id = :dealerId) AND " +
            "(:status IS NULL OR q.status = :status) AND " +
            "(:fromDate IS NULL OR q.quotationDate >= :fromDate) AND " +
            "(:toDate IS NULL OR q.quotationDate <= :toDate)")
    Page<Quotation> findQuotationsWithFilters(
            @Param("customerId") Long customerId,
            @Param("salesPersonId") Long salesPersonId,
            @Param("dealerId") Long dealerId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("SELECT q FROM Quotation q WHERE " +
            "q.validUntil < :today AND q.status = 'SENT'")
    List<Quotation> findExpiredQuotations(@Param("today") LocalDate today);

    @Query("SELECT q FROM Quotation q WHERE " +
            "q.validUntil BETWEEN :startDate AND :endDate AND " +
            "q.status = 'SENT'")
    List<Quotation> findQuotationsExpiringBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(q) FROM Quotation q WHERE " +
            "q.quotationDate >= :startDate AND " +
            "q.quotationDate <= :endDate AND " +
            "(:dealerId IS NULL OR q.dealer.id = :dealerId)")
    Long countQuotationsByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dealerId") Long dealerId
    );

    @Query("SELECT q FROM Quotation q " +
            "LEFT JOIN FETCH q.product " +
            "LEFT JOIN FETCH q.customer " +
            "LEFT JOIN FETCH q.salesPerson " +
            "LEFT JOIN FETCH q.quotationPromotions " +
            "WHERE q.id = :id")
    Optional<Quotation> findByIdWithDetails(@Param("id") Long id);

//    void deleteByQuotationBySalesOrderId(Long orderId);
}
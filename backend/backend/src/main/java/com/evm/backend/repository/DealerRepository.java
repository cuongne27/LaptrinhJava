package com.evm.backend.repository;

import com.evm.backend.entity.Dealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {

    /**
     * Tìm dealer theo email
     */
    Optional<Dealer> findByEmail(String email);

    /**
     * Check email có tồn tại không
     */
    boolean existsByEmail(String email);

    /**
     * Check email có tồn tại không (exclude current dealer khi update)
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Dealer d " +
            "WHERE d.email = :email AND d.id <> :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * Tìm tất cả dealers của brand
     */
    List<Dealer> findByBrandId(Integer brandId);

    /**
     * Tìm dealers theo brand với pagination
     */
    Page<Dealer> findByBrandId(Integer brandId, Pageable pageable);

    /**
     * Tìm dealers với filter
     */
    @Query("SELECT d FROM Dealer d WHERE " +
            "(:brandId IS NULL OR d.brand.id = :brandId) AND " +
            "(:searchKeyword IS NULL OR " +
            "LOWER(d.dealerName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(d.address) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) AND " +
            "(:dealerLevel IS NULL OR d.dealerLevel = :dealerLevel)")
    Page<Dealer> findDealersWithFilters(
            @Param("brandId") Integer brandId,
            @Param("searchKeyword") String searchKeyword,
            @Param("dealerLevel") String dealerLevel,
            Pageable pageable
    );

    /**
     * Đếm số users của dealer
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.dealer.id = :dealerId")
    Long countUsersByDealerId(@Param("dealerId") Long dealerId);

    /**
     * Đếm số vehicles của dealer
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.dealer.id = :dealerId")
    Long countVehiclesByDealerId(@Param("dealerId") Long dealerId);

    /**
     * Đếm số appointments của dealer
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dealer.id = :dealerId")
    Long countAppointmentsByDealerId(@Param("dealerId") Long dealerId);

    /**
     * Đếm số sell-in requests của dealer
     */
    @Query("SELECT COUNT(s) FROM SellInRequest s WHERE s.dealer.id = :dealerId")
    Long countSellInRequestsByDealerId(@Param("dealerId") Long dealerId);

    /**
     * Đếm số contracts của dealer
     */
    @Query("SELECT COUNT(c) FROM DealerContract c WHERE c.dealer.id = :dealerId")
    Long countContractsByDealerId(@Param("dealerId") Long dealerId);
}
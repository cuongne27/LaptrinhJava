package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    List<Dealer> findByBrand(Brand brand);

    List<Dealer> findByBrandId(Integer brandId);

    List<Dealer> findByDealerLevel(String dealerLevel);

    List<Dealer> findByDealerNameContainingIgnoreCase(String keyword);

    Optional<Dealer> findByEmailIgnoreCase(String email);

    @Query("SELECT d FROM Dealer d WHERE d.brand.id = :brandId AND d.dealerLevel = :level")
    List<Dealer> findByBrandAndLevel(@Param("brandId") Integer brandId, @Param("level") String level);

    @Query("SELECT d FROM Dealer d WHERE d.address LIKE %:city%")
    List<Dealer> findByCity(@Param("city") String city);

    long countByBrandId(Integer brandId);
}
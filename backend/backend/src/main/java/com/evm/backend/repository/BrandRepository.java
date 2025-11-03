package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    // ========== Find by Unique Fields ==========

    Optional<Brand> findByBrandName(String brandName);

    Optional<Brand> findByTaxCode(String taxCode);

    // ========== Existence Checks ==========

    boolean existsByBrandName(String brandName);

    boolean existsByTaxCode(String taxCode);

    // ========== Search ==========

    List<Brand> findByBrandNameContainingIgnoreCase(String keyword);

    @Query("SELECT b FROM Brand b WHERE b.brandName LIKE %:keyword% OR b.contactInfo LIKE %:keyword%")
    List<Brand> searchBrands(@Param("keyword") String keyword);

    // ========== Find by Location ==========

    @Query("SELECT b FROM Brand b WHERE b.headquartersAddress LIKE %:city%")
    List<Brand> findByHeadquartersCity(@Param("city") String city);

    // ========== Statistics ==========

    @Query("SELECT COUNT(b) FROM Brand b")
    long countAllBrands();

    // ========== With Relationships Count ==========

    @Query("SELECT b FROM Brand b LEFT JOIN FETCH b.dealers WHERE b.id = :id")
    Optional<Brand> findByIdWithDealers(@Param("id") Integer id);

    @Query("SELECT b FROM Brand b LEFT JOIN FETCH b.products WHERE b.id = :id")
    Optional<Brand> findByIdWithProducts(@Param("id") Integer id);

    @Query("SELECT b FROM Brand b LEFT JOIN FETCH b.dealerContracts WHERE b.id = :id")
    Optional<Brand> findByIdWithContracts(@Param("id") Integer id);

}
package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    /**
     * Tìm brand theo tên
     */
    Optional<Brand> findByBrandName(String brandName);

    /**
     * Check brand name có tồn tại không
     */
    boolean existsByBrandName(String brandName);

    /**
     * Check brand name có tồn tại không (exclude current brand khi update)
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Brand b " +
            "WHERE b.brandName = :brandName AND b.id <> :excludeId")
    boolean existsByBrandNameAndIdNot(@Param("brandName") String brandName, @Param("excludeId") Integer excludeId);

    /**
     * Tìm brand theo tax code
     */
    Optional<Brand> findByTaxCode(String taxCode);

    /**
     * Check tax code có tồn tại không
     */
    boolean existsByTaxCode(String taxCode);

    /**
     * Đếm số dealers của brand
     */
    @Query("SELECT COUNT(d) FROM Dealer d WHERE d.brand.id = :brandId")
    Long countDealersByBrandId(@Param("brandId") Integer brandId);

    /**
     * Đếm số products của brand
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    Long countProductsByBrandId(@Param("brandId") Integer brandId);

    /**
     * Đếm số users của brand
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.brand.id = :brandId")
    Long countUsersByBrandId(@Param("brandId") Integer brandId);

    /**
     * Đếm số contracts của brand
     */
    @Query("SELECT COUNT(c) FROM DealerContract c WHERE c.brand.id = :brandId")
    Long countContractsByBrandId(@Param("brandId") Integer brandId);
}
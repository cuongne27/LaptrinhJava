package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBrand(Brand brand);

    List<Product> findByBrandId(Integer brandId);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);

    List<Product> findByVersion(String version);

    @Query("SELECT p FROM Product p WHERE p.msrp BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId ORDER BY p.msrp DESC")
    List<Product> findByBrandOrderByPriceDesc(@Param("brandId") Integer brandId);

    @Query("SELECT p FROM Product p WHERE p.productName LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);

    long countByBrandId(Integer brandId);
}
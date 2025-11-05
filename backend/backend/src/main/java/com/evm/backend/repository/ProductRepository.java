package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    // Tìm sản phẩm active với filter
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:searchKeyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) AND " +
            "(:minPrice IS NULL OR p.msrp >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.msrp <= :maxPrice)")
    Page<Product> findProductsWithFilters(
            @Param("brandId") Long brandId,
            @Param("searchKeyword") String searchKeyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Tìm sản phẩm với thông tin chi tiết
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.vehicles v " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE p.id = :productId")
    Optional<Product> findByIdWithDetails(@Param("productId") Long productId);

    // Lấy các màu sắc có sẵn cho sản phẩm
    @Query("SELECT DISTINCT v.color FROM Vehicle v " +
            "WHERE v.product.id = :productId " +
            "AND v.status = 'AVAILABLE' " +
            "AND v.dealer.id = :dealerId")
    List<String> findAvailableColorsByProductAndDealer(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId
    );

    // Đếm số lượng xe có sẵn theo sản phẩm và dealer
    @Query("SELECT COUNT(v) FROM Vehicle v " +
            "WHERE v.product.id = :productId " +
            "AND v.status = 'AVAILABLE' " +
            "AND v.dealer.id = :dealerId")
    Long countAvailableVehiclesByProductAndDealer(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId
    );

    // Đếm số xe theo màu
    @Query("SELECT v.color, COUNT(v) FROM Vehicle v " +
            "WHERE v.product.id = :productId " +
            "AND v.status = 'AVAILABLE' " +
            "AND v.dealer.id = :dealerId " +
            "GROUP BY v.color")
    List<Object[]> countAvailableVehiclesByColorAndDealer(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId
    );
}
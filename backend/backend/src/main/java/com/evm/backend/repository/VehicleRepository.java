package com.evm.backend.repository;

import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.Product;
import com.evm.backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findByProduct(Product product);

    List<Vehicle> findByProductId(Long productId);

    List<Vehicle> findByDealer(Dealer dealer);

    List<Vehicle> findByDealerId(Long dealerId);

    List<Vehicle> findByStatus(String status);

    List<Vehicle> findByColor(String color);

    boolean existsByVin(String vin);

//    @Query("SELECT v FROM Vehicle v WHERE v.dealer.id = :dealerId AND v.status = :status")
//    List<Vehicle> findByDealerAndStatus(@Param("dealerId") Long dealerId,
//                                        @Param("status") String status);
//
//    @Query("SELECT v FROM Vehicle v WHERE v.product.id = :productId AND v.color = :color AND v.status = 'AVAILABLE'")
//    List<Vehicle> findAvailableByProductAndColor(@Param("productId") Long productId,
//                                                 @Param("color") String color);
//
//    @Query("SELECT v FROM Vehicle v WHERE v.warrantyExpiry < CURRENT_DATE")
//    List<Vehicle> findExpiredWarrantyVehicles();

    long countByDealerId(Long dealerId);

    long countByStatus(String status);
}
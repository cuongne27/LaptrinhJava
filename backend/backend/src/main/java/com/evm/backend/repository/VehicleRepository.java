package com.evm.backend.repository;

import com.evm.backend.entity.Vehicle;
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
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    /**
     * Tìm vehicle theo VIN
     */
    Optional<Vehicle> findByVin(String vin);

    /**
     * Check VIN có tồn tại không
     */
    boolean existsByVin(String vin);

    /**
     * Check VIN có tồn tại không (exclude current vehicle khi update)
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehicle v " +
            "WHERE v.vin = :vin AND v.id <> :excludeId")
    boolean existsByVinAndIdNot(@Param("vin") String vin, @Param("excludeId") String excludeId);

    /**
     * Tìm tất cả vehicles của product
     */
    List<Vehicle> findByProductId(Long productId);

    /**
     * Tìm tất cả vehicles của dealer
     */
    List<Vehicle> findByDealerId(Long dealerId);

    /**
     * Tìm vehicles theo status
     */
    List<Vehicle> findByStatus(String status);

    /**
     * Tìm available vehicles của dealer
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
            "v.dealer.id = :dealerId AND " +
            "v.status = 'AVAILABLE'")
    List<Vehicle> findAvailableVehiclesByDealer(@Param("dealerId") Long dealerId);

    /**
     * Tìm vehicles với filter
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:productId IS NULL OR v.product.id = :productId) AND " +
            "(:dealerId IS NULL OR v.dealer.id = :dealerId) AND " +
            "(:color IS NULL OR v.color = :color) AND " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:manufactureFromDate IS NULL OR v.manufactureDate >= :manufactureFromDate) AND " +
            "(:manufactureToDate IS NULL OR v.manufactureDate <= :manufactureToDate) AND " +
            "(:searchKeyword IS NULL OR " +
            "LOWER(v.id) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(v.vin) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(v.batterySerial) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))")
    Page<Vehicle> findVehiclesWithFilters(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId,
            @Param("color") String color,
            @Param("status") String status,
            @Param("manufactureFromDate") LocalDate manufactureFromDate,
            @Param("manufactureToDate") LocalDate manufactureToDate,
            @Param("searchKeyword") String searchKeyword,
            Pageable pageable
    );

    /**
     * Đếm số vehicles theo product và dealer
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE " +
            "v.product.id = :productId AND " +
            "v.dealer.id = :dealerId")
    Long countByProductAndDealer(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId
    );

    /**
     * Đếm số vehicles available theo product và dealer
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE " +
            "v.product.id = :productId AND " +
            "v.dealer.id = :dealerId AND " +
            "v.status = 'AVAILABLE'")
    Long countAvailableByProductAndDealer(
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId
    );

    /**
     * Đếm số support tickets của vehicle
     */
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.vehicle.id = :vehicleId")
    Long countSupportTicketsByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * Đếm số open tickets của vehicle
     */
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE " +
            "st.vehicle.id = :vehicleId AND " +
            "st.status IN ('OPEN', 'IN_PROGRESS', 'PENDING')")
    Long countOpenTicketsByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * Đếm số closed tickets của vehicle
     */
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE " +
            "st.vehicle.id = :vehicleId AND " +
            "st.status IN ('RESOLVED', 'CLOSED')")
    Long countClosedTicketsByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * Check xem vehicle đã có sales order chưa
     */
    @Query("SELECT CASE WHEN COUNT(so) > 0 THEN true ELSE false END FROM SalesOrder so " +
            "WHERE so.vehicle.id = :vehicleId")
    boolean hasSalesOrder(@Param("vehicleId") String vehicleId);

    /**
     * Lấy sales order của vehicle (nếu có)
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.vehicle.id = :vehicleId")
    Optional<Object> findSalesOrderByVehicleId(@Param("vehicleId") String vehicleId);
}
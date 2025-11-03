package com.evm.backend.repository;

import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.Vehicle;
import com.evm.backend.entity.VehicleServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleServiceHistoryRepository extends JpaRepository<VehicleServiceHistory, Long> {
    List<VehicleServiceHistory> findByVehicle(Vehicle vehicle);

    List<VehicleServiceHistory> findByVehicleId(String vehicleId);

    List<VehicleServiceHistory> findByVehicleIdOrderByServiceDateDesc(String vehicleId);

    List<VehicleServiceHistory> findByDealer(Dealer dealer);

    List<VehicleServiceHistory> findByDealerId(Long dealerId);

//    List<VehicleServiceHistory> findByServiceType(String serviceType);

//    @Query("SELECT vsh FROM VehicleServiceHistory vsh WHERE vsh.vehicle.id = :vehicleId AND vsh.serviceDate BETWEEN :startDate AND :endDate")
//    List<VehicleServiceHistory> findByVehicleAndDateRange(@Param("vehicleId") Long vehicleId,
//                                                          @Param("startDate") LocalDate startDate,
//                                                          @Param("endDate") LocalDate endDate);
//
//    @Query("SELECT vsh FROM VehicleServiceHistory vsh WHERE vsh.dealer.id = :dealerId AND vsh.serviceDate = :serviceDate")
//    List<VehicleServiceHistory> findByDealerAndDate(@Param("dealerId") Long dealerId,
//                                                    @Param("serviceDate") LocalDate serviceDate);
//
//    @Query("SELECT vsh FROM VehicleServiceHistory vsh WHERE vsh.vehicle.id = :vehicleId ORDER BY vsh.odometerReading DESC")
//    List<VehicleServiceHistory> findByVehicleOrderByOdometerDesc(@Param("vehicleId") Long vehicleId);
}
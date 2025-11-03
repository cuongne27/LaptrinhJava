package com.evm.backend.repository;

import com.evm.backend.entity.VehicleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleHistoryRepository extends JpaRepository<VehicleHistory, Long> {

}
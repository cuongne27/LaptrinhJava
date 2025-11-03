package com.evm.backend.repository;

import com.evm.backend.entity.Customer;
import com.evm.backend.entity.SalesOrder;
import com.evm.backend.entity.User;
import com.evm.backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByCustomer(Customer customer);

    List<SalesOrder> findByCustomerId(Long customerId);

    List<SalesOrder> findBySalesPerson(User salesPerson);

    List<SalesOrder> findBySalesPersonId(Long salesPersonId);

    List<SalesOrder> findByVehicle(Vehicle vehicle);

    List<SalesOrder> findByStatus(String status);

    @Query("SELECT so FROM SalesOrder so WHERE so.orderDate BETWEEN :startDate AND :endDate")
    List<SalesOrder> findByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT so FROM SalesOrder so WHERE so.salesPerson.dealer.id = :dealerId")
    List<SalesOrder> findByDealerId(@Param("dealerId") Long dealerId);

    @Query("SELECT so FROM SalesOrder so WHERE so.salesPerson.dealer.id = :dealerId AND so.orderDate BETWEEN :startDate AND :endDate")
    List<SalesOrder> findByDealerAndDateRange(@Param("dealerId") Long dealerId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(so.totalPrice) FROM SalesOrder so WHERE so.salesPerson.dealer.id = :dealerId AND so.status = 'COMPLETED'")
    BigDecimal getTotalRevenueByDealer(@Param("dealerId") Long dealerId);

    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.salesPerson.dealer.id = :dealerId AND so.orderDate BETWEEN :startDate AND :endDate")
    long countByDealerAndDateRange(@Param("dealerId") Long dealerId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT so FROM SalesOrder so WHERE so.status = :status ORDER BY so.orderDate DESC")
    List<SalesOrder> findByStatusOrderByDateDesc(@Param("status") String status);
}
// ==================== 10. CUSTOMER REPOSITORY ====================
package com.evm.backend.repository;

import com.evm.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    List<Customer> findByPhoneNumber(String phoneNumber);

    List<Customer> findByCustomerType(String customerType);

    List<Customer> findByFullNameContainingIgnoreCase(String keyword);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT c FROM Customer c WHERE c.fullName LIKE %:keyword% OR c.phoneNumber LIKE %:keyword% OR c.email LIKE %:keyword%")
    List<Customer> searchCustomers(@Param("keyword") String keyword);

    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :startDate")
    List<Customer> findNewCustomers(@Param("startDate") java.time.OffsetDateTime startDate);

    @Query("SELECT c FROM Customer c WHERE c.address LIKE %:city%")
    List<Customer> findByCity(@Param("city") String city);

    long countByCustomerType(String customerType);
}
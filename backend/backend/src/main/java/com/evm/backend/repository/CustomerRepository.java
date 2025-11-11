package com.evm.backend.repository;

import com.evm.backend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Tìm customer theo email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Tìm customer theo phone number
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /**
     * Check email có tồn tại không
     */
    boolean existsByEmail(String email);

    /**
     * Check phone number có tồn tại không
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check email có tồn tại không (exclude current customer khi update)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.email = :email AND c.id <> :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * Check phone number có tồn tại không (exclude current customer khi update)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.phoneNumber = :phoneNumber AND c.id <> :excludeId")
    boolean existsByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("excludeId") Long excludeId);

    /**
     * Tìm customers với filter
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "(:searchKeyword IS NULL OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) AND " +
            "(:customerType IS NULL OR c.customerType = :customerType)")
    Page<Customer> findCustomersWithFilters(
            @Param("searchKeyword") String searchKeyword,
            @Param("customerType") String customerType,
            Pageable pageable
    );

    /**
     * Đếm số sales orders của customer
     */
    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.customer.id = :customerId")
    Long countOrdersByCustomerId(@Param("customerId") Long customerId);

    /**
     * Đếm số support tickets của customer
     */
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.customer.id = :customerId")
    Long countSupportTicketsByCustomerId(@Param("customerId") Long customerId);

    /**
     * Đếm số open tickets của customer
     */
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE " +
            "st.customer.id = :customerId AND " +
            "st.status IN ('OPEN', 'IN_PROGRESS', 'PENDING')")
    Long countOpenTicketsByCustomerId(@Param("customerId") Long customerId);

    /**
     * Đếm số closed tickets của customer
     */
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE " +
            "st.customer.id = :customerId AND " +
            "st.status IN ('RESOLVED', 'CLOSED')")
    Long countClosedTicketsByCustomerId(@Param("customerId") Long customerId);
}
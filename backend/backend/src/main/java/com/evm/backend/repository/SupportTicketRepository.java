package com.evm.backend.repository;

import com.evm.backend.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SupportTicket entity
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>, JpaSpecificationExecutor<SupportTicket> {

    /**
     * Find ticket by ID with all details
     */
    @Query("SELECT t FROM SupportTicket t " +
            "LEFT JOIN FETCH t.customer c " +
            "LEFT JOIN FETCH t.assignedUser u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH t.salesOrder o " +
            "LEFT JOIN FETCH t.vehicle v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE t.id = :ticketId")
    Optional<SupportTicket> findByIdWithDetails(@Param("ticketId") Long ticketId);

    /**
     * Find tickets by customer ID
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.customer.id = :customerId ORDER BY t.createdAt DESC")
    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    /**
     * Find tickets by assigned user ID
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.assignedUser.id = :userId ORDER BY t.createdAt DESC")
    List<SupportTicket> findByAssignedUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find tickets by sales order ID
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.salesOrder.id = :orderId ORDER BY t.createdAt DESC")
    List<SupportTicket> findBySalesOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    /**
     * Find tickets by vehicle ID
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.vehicle.id = :vehicleId ORDER BY t.createdAt DESC")
    List<SupportTicket> findByVehicleIdOrderByCreatedAtDesc(@Param("vehicleId") String vehicleId);

    /**
     * Find tickets by status in list
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.status IN :statuses ORDER BY t.createdAt DESC")
    List<SupportTicket> findByStatusInOrderByCreatedAtDesc(@Param("statuses") List<String> statuses);

    /**
     * Find pending tickets (not assigned)
     */
    @Query("SELECT t FROM SupportTicket t " +
            "WHERE t.status = :status AND t.assignedUser IS NULL " +
            "ORDER BY t.createdAt DESC")
    List<SupportTicket> findByStatusAndAssignedUserIsNullOrderByCreatedAtDesc(@Param("status") String status);

    /**
     * Find my tickets (assigned to user and status is OPEN or IN_PROGRESS)
     */
    @Query("SELECT t FROM SupportTicket t " +
            "WHERE t.assignedUser.id = :userId " +
            "AND t.status IN :statuses " +
            "ORDER BY t.createdAt DESC")
    List<SupportTicket> findByAssignedUserIdAndStatusInOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("statuses") List<String> statuses
    );

    /**
     * Count tickets by status
     */
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status = :status")
    Long countByStatus(@Param("status") String status);

    /**
     * Find tickets by status
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(@Param("status") String status);

    /**
     * Find unassigned tickets
     */
    @Query("SELECT t FROM SupportTicket t WHERE t.assignedUser IS NULL ORDER BY t.createdAt DESC")
    List<SupportTicket> findUnassignedTickets();

    /**
     * Find tickets by customer and status
     */
    @Query("SELECT t FROM SupportTicket t " +
            "WHERE t.customer.id = :customerId " +
            "AND t.status = :status " +
            "ORDER BY t.createdAt DESC")
    List<SupportTicket> findByCustomerIdAndStatus(
            @Param("customerId") Long customerId,
            @Param("status") String status
    );

    /**
     * Find overdue tickets (open for more than N days)
     */
    @Query("SELECT t FROM SupportTicket t " +
            "WHERE t.status IN ('OPEN', 'IN_PROGRESS') " +
            "AND t.createdAt < :beforeDate " +
            "ORDER BY t.createdAt ASC")
    List<SupportTicket> findOverdueTickets(@Param("beforeDate") java.time.OffsetDateTime beforeDate);

    /**
     * Count tickets by customer
     */
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.customer.id = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

    /**
     * Count tickets by assigned user
     */
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.assignedUser.id = :userId")
    Long countByAssignedUserId(@Param("userId") Long userId);

    /**
     * Find recent tickets (last N days)
     */
    @Query("SELECT t FROM SupportTicket t " +
            "WHERE t.createdAt >= :fromDate " +
            "ORDER BY t.createdAt DESC")
    List<SupportTicket> findRecentTickets(@Param("fromDate") java.time.OffsetDateTime fromDate);
}
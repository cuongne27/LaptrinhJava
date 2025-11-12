package com.evm.backend.repository;

import com.evm.backend.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Tìm tất cả appointments của customer
     */
    List<Appointment> findByCustomerId(Long customerId);

    /**
     * Tìm tất cả appointments của staff
     */
    List<Appointment> findByStaffUserId(Long staffUserId);

    /**
     * Tìm tất cả appointments của dealer
     */
    List<Appointment> findByDealerId(Long dealerId);

    /**
     * Tìm tất cả appointments của product
     */
    List<Appointment> findByProductId(Long productId);

    /**
     * Tìm appointments với filter
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "(:customerId IS NULL OR a.customer.id = :customerId) AND " +
            "(:staffUserId IS NULL OR a.staffUser.id = :staffUserId) AND " +
            "(:productId IS NULL OR a.product.id = :productId) AND " +
            "(:dealerId IS NULL OR a.dealer.id = :dealerId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:fromDate IS NULL OR a.appointmentTime >= :fromDate) AND " +
            "(:toDate IS NULL OR a.appointmentTime <= :toDate)")
    Page<Appointment> findAppointmentsWithFilters(
            @Param("customerId") Long customerId,
            @Param("staffUserId") Long staffUserId,
            @Param("productId") Long productId,
            @Param("dealerId") Long dealerId,
            @Param("status") String status,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            Pageable pageable
    );

    /**
     * Tìm upcoming appointments (chưa đến giờ hẹn)
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.appointmentTime > :currentTime AND " +
            "a.status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')")
    List<Appointment> findUpcomingAppointments(
            @Param("currentTime") OffsetDateTime currentTime
    );

    /**
     * Tìm appointments hôm nay
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.appointmentTime >= :startOfDay AND " +
            "a.appointmentTime < :endOfDay")
    List<Appointment> findAppointmentsToday(
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );

    /**
     * Tìm appointments của staff trong khoảng thời gian
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.staffUser.id = :staffUserId AND " +
            "a.appointmentTime BETWEEN :startTime AND :endTime AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Appointment> findStaffAppointmentsInTimeRange(
            @Param("staffUserId") Long staffUserId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );

    /**
     * Check xem staff có available trong khoảng thời gian không
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN false ELSE true END FROM Appointment a WHERE " +
            "a.staffUser.id = :staffUserId AND " +
            "a.appointmentTime BETWEEN :startTime AND :endTime AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    boolean isStaffAvailable(
            @Param("staffUserId") Long staffUserId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );

    /**
     * Đếm appointments theo status
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
            "a.status = :status AND " +
            "(:dealerId IS NULL OR a.dealer.id = :dealerId)")
    Long countByStatus(
            @Param("status") String status,
            @Param("dealerId") Long dealerId
    );

    /**
     * Đếm upcoming appointments của customer
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
            "a.customer.id = :customerId AND " +
            "a.appointmentTime > :currentTime AND " +
            "a.status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')")
    Long countUpcomingByCustomer(
            @Param("customerId") Long customerId,
            @Param("currentTime") OffsetDateTime currentTime
    );
}
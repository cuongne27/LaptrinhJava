package com.evm.backend.repository;

import com.evm.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // ========== Find by Relationships ==========

    List<Appointment> findByCustomer(Customer customer);

    List<Appointment> findByCustomerId(Long customerId);

    List<Appointment> findByStaffUser(User staffUser);

    List<Appointment> findByStaffUserId(Long staffUserId);

    List<Appointment> findByDealer(Dealer dealer);

    List<Appointment> findByDealerId(Long dealerId);

    List<Appointment> findByProduct(Product product);

    List<Appointment> findByProductId(Long productId);

    // ========== Find by Status ==========

    List<Appointment> findByStatus(String status);

    List<Appointment> findByStatusOrderByAppointmentTimeAsc(String status);

    @Query("SELECT a FROM Appointment a WHERE a.dealer.id = :dealerId AND a.status = :status ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDealerAndStatus(@Param("dealerId") Long dealerId,
                                            @Param("status") String status);

    // ========== Find by Time Range ==========

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :startTime AND :endTime ORDER BY a.appointmentTime ASC")
    List<Appointment> findByTimeRange(@Param("startTime") OffsetDateTime startTime,
                                      @Param("endTime") OffsetDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.dealer.id = :dealerId AND a.appointmentTime BETWEEN :startTime AND :endTime ORDER BY a.appointmentTime ASC")
    List<Appointment> findByDealerAndTimeRange(@Param("dealerId") Long dealerId,
                                               @Param("startTime") OffsetDateTime startTime,
                                               @Param("endTime") OffsetDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.staffUser.id = :staffUserId AND a.appointmentTime BETWEEN :startTime AND :endTime ORDER BY a.appointmentTime ASC")
    List<Appointment> findByStaffUserAndTimeRange(@Param("staffUserId") Long staffUserId,
                                                  @Param("startTime") OffsetDateTime startTime,
                                                  @Param("endTime") OffsetDateTime endTime);

    // ========== Upcoming Appointments ==========

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime >= :currentTime AND a.status = :status ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("currentTime") OffsetDateTime currentTime,
                                               @Param("status") String status);

    @Query("SELECT a FROM Appointment a WHERE a.staffUser.id = :staffUserId AND a.appointmentTime >= :currentTime AND a.status = :status ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsByStaff(@Param("staffUserId") Long staffUserId,
                                                      @Param("currentTime") OffsetDateTime currentTime,
                                                      @Param("status") String status);

    @Query("SELECT a FROM Appointment a WHERE a.dealer.id = :dealerId AND a.appointmentTime >= :currentTime AND a.status = :status ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsByDealer(@Param("dealerId") Long dealerId,
                                                       @Param("currentTime") OffsetDateTime currentTime,
                                                       @Param("status") String status);

    @Query("SELECT a FROM Appointment a WHERE a.customer.id = :customerId AND a.appointmentTime >= :currentTime ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsByCustomer(@Param("customerId") Long customerId,
                                                         @Param("currentTime") OffsetDateTime currentTime);

    // ========== Today's Appointments ==========

    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentTime) = CURRENT_DATE AND a.dealer.id = :dealerId ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAppointmentsByDealer(@Param("dealerId") Long dealerId);

    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentTime) = CURRENT_DATE AND a.staffUser.id = :staffUserId ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAppointmentsByStaff(@Param("staffUserId") Long staffUserId);

    // ========== Past/Overdue Appointments ==========

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime < :currentTime AND a.status = 'SCHEDULED' ORDER BY a.appointmentTime DESC")
    List<Appointment> findOverdueAppointments(@Param("currentTime") OffsetDateTime currentTime);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime < :currentTime AND a.status = 'COMPLETED' ORDER BY a.appointmentTime DESC")
    List<Appointment> findCompletedAppointments(@Param("currentTime") OffsetDateTime currentTime);

    // ========== Unassigned Appointments ==========

    @Query("SELECT a FROM Appointment a WHERE a.staffUser IS NULL AND a.status = 'SCHEDULED' ORDER BY a.appointmentTime ASC")
    List<Appointment> findUnassignedAppointments();

    @Query("SELECT a FROM Appointment a WHERE a.dealer.id = :dealerId AND a.staffUser IS NULL AND a.status = 'SCHEDULED' ORDER BY a.appointmentTime ASC")
    List<Appointment> findUnassignedAppointmentsByDealer(@Param("dealerId") Long dealerId);

    // ========== Product Test Drive Appointments ==========

    @Query("SELECT a FROM Appointment a WHERE a.product.id = :productId AND a.status = :status ORDER BY a.appointmentTime DESC")
    List<Appointment> findTestDrivesByProduct(@Param("productId") Long productId,
                                              @Param("status") String status);

    @Query("SELECT a FROM Appointment a WHERE a.product.id = :productId AND a.appointmentTime BETWEEN :startTime AND :endTime")
    List<Appointment> findTestDrivesByProductAndTimeRange(@Param("productId") Long productId,
                                                          @Param("startTime") OffsetDateTime startTime,
                                                          @Param("endTime") OffsetDateTime endTime);

    // ========== Check Availability ==========

    @Query("SELECT a FROM Appointment a WHERE a.staffUser.id = :staffUserId AND a.appointmentTime BETWEEN :startTime AND :endTime AND a.status != 'CANCELLED'")
    List<Appointment> findConflictingAppointments(@Param("staffUserId") Long staffUserId,
                                                  @Param("startTime") OffsetDateTime startTime,
                                                  @Param("endTime") OffsetDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.product.id = :productId AND a.appointmentTime BETWEEN :startTime AND :endTime AND a.status = 'SCHEDULED'")
    List<Appointment> findProductBookingConflicts(@Param("productId") Long productId,
                                                  @Param("startTime") OffsetDateTime startTime,
                                                  @Param("endTime") OffsetDateTime endTime);

    // ========== Statistics & Counts ==========

    long countByDealerId(Long dealerId);

    long countByDealerIdAndStatus(Long dealerId, String status);

    long countByStaffUserId(Long staffUserId);

    long countByCustomerId(Long customerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE DATE(a.appointmentTime) = CURRENT_DATE AND a.dealer.id = :dealerId")
    long countTodayAppointmentsByDealer(@Param("dealerId") Long dealerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentTime >= :currentTime AND a.status = 'SCHEDULED'")
    long countUpcomingAppointments(@Param("currentTime") OffsetDateTime currentTime);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentTime < :currentTime AND a.status = 'SCHEDULED'")
    long countOverdueAppointments(@Param("currentTime") OffsetDateTime currentTime);

    // ========== Weekly/Monthly Reports ==========

    @Query("SELECT DATE(a.appointmentTime) as date, COUNT(a) as count FROM Appointment a WHERE a.dealer.id = :dealerId AND a.appointmentTime BETWEEN :startDate AND :endDate GROUP BY DATE(a.appointmentTime) ORDER BY date")
    List<Object[]> getAppointmentStatsByDealer(@Param("dealerId") Long dealerId,
                                               @Param("startDate") OffsetDateTime startDate,
                                               @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a WHERE a.dealer.id = :dealerId GROUP BY a.status")
    List<Object[]> getAppointmentStatusBreakdown(@Param("dealerId") Long dealerId);

    // ========== Customer History ==========

    @Query("SELECT a FROM Appointment a WHERE a.customer.id = :customerId ORDER BY a.appointmentTime DESC")
    List<Appointment> findCustomerAppointmentHistory(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.customer.id = :customerId AND a.status = 'COMPLETED'")
    long countCompletedAppointmentsByCustomer(@Param("customerId") Long customerId);
}
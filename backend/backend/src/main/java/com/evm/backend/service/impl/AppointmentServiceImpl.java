package com.evm.backend.service.impl;

import com.evm.backend.dto.request.AppointmentFilterRequest;
import com.evm.backend.dto.request.AppointmentRequest;
import com.evm.backend.dto.response.AppointmentDetailResponse;
import com.evm.backend.dto.response.AppointmentListResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DealerRepository dealerRepository;

    @Override
    public Page<AppointmentListResponse> getAllAppointments(AppointmentFilterRequest filterRequest) {
        Pageable pageable = buildPageable(filterRequest);
        Page<Appointment> appointments = appointmentRepository.findAppointmentsWithFilters(
                filterRequest.getCustomerId(), filterRequest.getStaffUserId(),
                filterRequest.getProductId(), filterRequest.getDealerId(),
                filterRequest.getStatus(), filterRequest.getFromDate(),
                filterRequest.getToDate(), pageable);
        return appointments.map(this::convertToListResponse);
    }

    @Override
    public List<AppointmentListResponse> getUpcomingAppointments() {
        return appointmentRepository.findUpcomingAppointments(OffsetDateTime.now())
                .stream().map(this::convertToListResponse).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentListResponse> getTodayAppointments() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfDay = now.toLocalDate().atStartOfDay().atZone(now.getOffset()).toOffsetDateTime();
        OffsetDateTime endOfDay = startOfDay.plusDays(1);
        return appointmentRepository.findAppointmentsToday(startOfDay, endOfDay)
                .stream().map(this::convertToListResponse).collect(Collectors.toList());
    }

    @Override
    public AppointmentDetailResponse getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));
        return convertToDetailResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentDetailResponse createAppointment(AppointmentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        User staffUser = null;
        if (request.getStaffUserId() != null) {
            staffUser = userRepository.findById(request.getStaffUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));

            // Check staff availability
            OffsetDateTime startTime = request.getAppointmentTime().minusMinutes(30);
            OffsetDateTime endTime = request.getAppointmentTime().plusMinutes(30);
            if (!appointmentRepository.isStaffAvailable(staffUser.getId(), startTime, endTime)) {
                throw new IllegalStateException("Staff is not available at this time");
            }
        }

        Appointment appointment = Appointment.builder()
                .appointmentTime(request.getAppointmentTime())
                .status(request.getStatus() != null ? request.getStatus() : "SCHEDULED")
                .notes(request.getNotes())
                .customer(customer)
                .staffUser(staffUser)
                .product(product)
                .dealer(dealer)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created: {}", saved.getId());
        return convertToDetailResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentDetailResponse updateAppointment(Long appointmentId, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        User staffUser = null;
        if (request.getStaffUserId() != null) {
            staffUser = userRepository.findById(request.getStaffUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
        }

        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(request.getStatus());
        appointment.setNotes(request.getNotes());
        appointment.setCustomer(customer);
        appointment.setStaffUser(staffUser);
        appointment.setProduct(product);
        appointment.setDealer(dealer);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment updated: {}", appointmentId);
        return convertToDetailResponse(updated);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new IllegalStateException("Cannot cancel appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled: {}", appointmentId);
    }

    @Override
    @Transactional
    public void deleteAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Appointment not found");
        }
        appointmentRepository.deleteById(appointmentId);
        log.info("Appointment deleted: {}", appointmentId);
    }

    private AppointmentListResponse convertToListResponse(Appointment a) {
        OffsetDateTime now = OffsetDateTime.now();
        boolean isUpcoming = a.getAppointmentTime().isAfter(now);
        boolean isToday = a.getAppointmentTime().toLocalDate().equals(now.toLocalDate());
        Long hoursUntil = isUpcoming ? Duration.between(now, a.getAppointmentTime()).toHours() : null;

        return AppointmentListResponse.builder()
                .id(a.getId()).appointmentTime(a.getAppointmentTime()).status(a.getStatus())
                .customerId(a.getCustomer() != null ? a.getCustomer().getId() : null)
                .customerName(a.getCustomer() != null ? a.getCustomer().getFullName() : null)
                .customerPhone(a.getCustomer() != null ? a.getCustomer().getPhoneNumber() : null)
                .staffUserId(a.getStaffUser() != null ? a.getStaffUser().getId() : null)
                .staffName(a.getStaffUser() != null ? a.getStaffUser().getFullName() : null)
                .productId(a.getProduct() != null ? a.getProduct().getId() : null)
                .productName(a.getProduct() != null ? a.getProduct().getProductName() : null)
                .dealerId(a.getDealer() != null ? a.getDealer().getId() : null)
                .dealerName(a.getDealer() != null ? a.getDealer().getDealerName() : null)
                .isUpcoming(isUpcoming).isToday(isToday).hoursUntil(hoursUntil)
                .build();
    }

    private AppointmentDetailResponse convertToDetailResponse(Appointment a) {
        OffsetDateTime now = OffsetDateTime.now();
        boolean isUpcoming = a.getAppointmentTime().isAfter(now);
        boolean isToday = a.getAppointmentTime().toLocalDate().equals(now.toLocalDate());
        Long hoursUntil = isUpcoming ? Duration.between(now, a.getAppointmentTime()).toHours() : null;
        boolean canCancel = isUpcoming && hoursUntil != null && hoursUntil > 24;

        return AppointmentDetailResponse.builder()
                .id(a.getId()).appointmentTime(a.getAppointmentTime())
                .status(a.getStatus()).notes(a.getNotes())
                .customerId(a.getCustomer() != null ? a.getCustomer().getId() : null)
                .customerName(a.getCustomer() != null ? a.getCustomer().getFullName() : null)
                .customerPhone(a.getCustomer() != null ? a.getCustomer().getPhoneNumber() : null)
                .customerEmail(a.getCustomer() != null ? a.getCustomer().getEmail() : null)
                .staffUserId(a.getStaffUser() != null ? a.getStaffUser().getId() : null)
                .staffName(a.getStaffUser() != null ? a.getStaffUser().getFullName() : null)
                .staffEmail(a.getStaffUser() != null ? a.getStaffUser().getEmail() : null)
                .productId(a.getProduct() != null ? a.getProduct().getId() : null)
                .productName(a.getProduct() != null ? a.getProduct().getProductName() : null)
                .productVersion(a.getProduct() != null ? a.getProduct().getVersion() : null)
                .productImageUrl(a.getProduct() != null ? a.getProduct().getImageUrl() : null)
                .dealerId(a.getDealer() != null ? a.getDealer().getId() : null)
                .dealerName(a.getDealer() != null ? a.getDealer().getDealerName() : null)
                .dealerAddress(a.getDealer() != null ? a.getDealer().getAddress() : null)
                .dealerPhone(a.getDealer() != null ? a.getDealer().getPhoneNumber() : null)
                .isUpcoming(isUpcoming).isToday(isToday).hoursUntil(hoursUntil).canCancel(canCancel)
                .build();
    }

    private Pageable buildPageable(AppointmentFilterRequest req) {
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 20;
        if (size > 100) size = 100;

        Sort sort = Sort.unsorted();
        if (req.getSortBy() != null) {
            switch (req.getSortBy()) {
                case "time_asc": sort = Sort.by(Sort.Direction.ASC, "appointmentTime"); break;
                case "time_desc": sort = Sort.by(Sort.Direction.DESC, "appointmentTime"); break;
                case "status_asc": sort = Sort.by(Sort.Direction.ASC, "status"); break;
                case "status_desc": sort = Sort.by(Sort.Direction.DESC, "status"); break;
                default: sort = Sort.by(Sort.Direction.ASC, "appointmentTime");
            }
        }
        return PageRequest.of(page, size, sort);
    }
}
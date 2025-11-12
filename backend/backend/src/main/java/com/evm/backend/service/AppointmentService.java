package com.evm.backend.service;

import com.evm.backend.dto.request.AppointmentFilterRequest;
import com.evm.backend.dto.request.AppointmentRequest;
import com.evm.backend.dto.response.AppointmentDetailResponse;
import com.evm.backend.dto.response.AppointmentListResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface AppointmentService {
    Page<AppointmentListResponse> getAllAppointments(AppointmentFilterRequest filterRequest);
    List<AppointmentListResponse> getUpcomingAppointments();
    List<AppointmentListResponse> getTodayAppointments();
    AppointmentDetailResponse getAppointmentById(Long appointmentId);
    AppointmentDetailResponse createAppointment(AppointmentRequest request);
    AppointmentDetailResponse updateAppointment(Long appointmentId, AppointmentRequest request);
    void cancelAppointment(Long appointmentId);
    void deleteAppointment(Long appointmentId);
}
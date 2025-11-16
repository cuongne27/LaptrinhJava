package com.evm.backend.service;

import com.evm.backend.dto.request.SupportTicketFilterRequest;
import com.evm.backend.dto.request.SupportTicketRequest;
import com.evm.backend.dto.response.SupportTicketDetailResponse;
import com.evm.backend.dto.response.SupportTicketListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Support Ticket operations
 * Quản lý tickets chăm sóc khách hàng
 */
public interface SupportTicketService {

    /**
     * Get all tickets with filtering and pagination
     */
    Page<SupportTicketListResponse> getAllTickets(SupportTicketFilterRequest filterRequest);

    /**
     * Get tickets by customer
     */
    List<SupportTicketListResponse> getTicketsByCustomer(Long customerId);

    /**
     * Get tickets by assigned user
     */
    List<SupportTicketListResponse> getTicketsByAssignedUser(Long userId);

    /**
     * Get tickets by sales order
     */
    List<SupportTicketListResponse> getTicketsBySalesOrder(Long orderId);

    /**
     * Get tickets by vehicle
     */
    List<SupportTicketListResponse> getTicketsByVehicle(String vehicleId);

    /**
     * Get open tickets
     */
    List<SupportTicketListResponse> getOpenTickets();

    /**
     * Get pending tickets (chưa assign)
     */
    List<SupportTicketListResponse> getPendingTickets();

    /**
     * Get my tickets (assigned to current user)
     */
    List<SupportTicketListResponse> getMyTickets(Long userId);

    /**
     * Get ticket detail by ID
     */
    SupportTicketDetailResponse getTicketById(Long ticketId);

    /**
     * Create new ticket
     */
    SupportTicketDetailResponse createTicket(SupportTicketRequest request);

    /**
     * Update ticket
     */
    SupportTicketDetailResponse updateTicket(Long ticketId, SupportTicketRequest request);

    /**
     * Assign ticket to user
     */
    SupportTicketDetailResponse assignTicket(Long ticketId, Long userId);

    /**
     * Update ticket status
     */
    SupportTicketDetailResponse updateTicketStatus(Long ticketId, String status);

    /**
     * Close ticket
     */
    SupportTicketDetailResponse closeTicket(Long ticketId);

    /**
     * Reopen ticket
     */
    SupportTicketDetailResponse reopenTicket(Long ticketId);

    /**
     * Delete ticket
     */
    void deleteTicket(Long ticketId);

    /**
     * Get ticket statistics
     */
    java.util.Map<String, Long> getTicketStatistics();
}
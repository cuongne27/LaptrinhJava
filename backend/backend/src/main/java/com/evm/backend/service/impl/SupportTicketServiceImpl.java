package com.evm.backend.service.impl;

import com.evm.backend.dto.request.SupportTicketFilterRequest;
import com.evm.backend.dto.request.SupportTicketRequest;
import com.evm.backend.dto.response.SupportTicketDetailResponse;
import com.evm.backend.dto.response.SupportTicketListResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.SupportTicketService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketListResponse> getAllTickets(SupportTicketFilterRequest filterRequest) {
        log.info("Getting all tickets with filter: {}", filterRequest);

        Specification<SupportTicket> spec = buildSpecification(filterRequest);
        Pageable pageable = buildPageable(filterRequest);

        Page<SupportTicket> tickets = supportTicketRepository.findAll(spec, pageable);

        return tickets.map(this::convertToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getTicketsByCustomer(Long customerId) {
        log.info("Getting tickets for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        List<SupportTicket> tickets = supportTicketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getTicketsByAssignedUser(Long userId) {
        log.info("Getting tickets for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<SupportTicket> tickets = supportTicketRepository.findByAssignedUserIdOrderByCreatedAtDesc(userId);

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getTicketsBySalesOrder(Long orderId) {
        log.info("Getting tickets for order: {}", orderId);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        List<SupportTicket> tickets = supportTicketRepository.findBySalesOrderIdOrderByCreatedAtDesc(orderId);

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getTicketsByVehicle(String vehicleId) {
        log.info("Getting tickets for vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        List<SupportTicket> tickets = supportTicketRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getOpenTickets() {
        log.info("Getting open tickets");

        List<SupportTicket> tickets = supportTicketRepository.findByStatusInOrderByCreatedAtDesc(
                Arrays.asList("OPEN", "IN_PROGRESS")
        );

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getPendingTickets() {
        log.info("Getting pending tickets");

        List<SupportTicket> tickets = supportTicketRepository.findByStatusAndAssignedUserIsNullOrderByCreatedAtDesc("PENDING");

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketListResponse> getMyTickets(Long userId) {
        log.info("Getting my tickets for user: {}", userId);

        List<SupportTicket> tickets = supportTicketRepository.findByAssignedUserIdAndStatusInOrderByCreatedAtDesc(
                userId, Arrays.asList("OPEN", "IN_PROGRESS")
        );

        return tickets.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketDetailResponse getTicketById(Long ticketId) {
        log.info("Getting ticket by id: {}", ticketId);

        SupportTicket ticket = supportTicketRepository.findByIdWithDetails(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        return convertToDetailResponse(ticket);
    }

    @Override
    public SupportTicketDetailResponse createTicket(SupportTicketRequest request) {
        log.info("Creating ticket for customer: {}", request.getCustomerId());

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        // Get assigned user (if specified)
        User assignedUser = null;
        if (request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedUserId()));
        }

        // Get sales order (if specified)
        SalesOrder salesOrder = null;
        if (request.getSalesOrderId() != null) {
            salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getSalesOrderId()));
        }

        // Get vehicle (if specified)
        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.getVehicleId()));
        }

        // Create ticket
        SupportTicket ticket = SupportTicket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "OPEN")
                .priority(request.getPriority())
                .category(request.getCategory())
                .customer(customer)
                .assignedUser(assignedUser)
                .salesOrder(salesOrder)
                .vehicle(vehicle)
                .createdAt(OffsetDateTime.now())
                .build();

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        log.info("Ticket created successfully: {}", savedTicket.getId());

        return convertToDetailResponse(savedTicket);
    }

    @Override
    public SupportTicketDetailResponse updateTicket(Long ticketId, SupportTicketRequest request) {
        log.info("Updating ticket: {}", ticketId);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        // Check if ticket is closed
        if ("CLOSED".equals(ticket.getStatus())) {
            throw new BadRequestException("Cannot update closed ticket");
        }

        // Update customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));
        ticket.setCustomer(customer);

        // Update assigned user
        if (request.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedUserId()));
            ticket.setAssignedUser(assignedUser);
        } else {
            ticket.setAssignedUser(null);
        }

        // Update sales order
        if (request.getSalesOrderId() != null) {
            SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getSalesOrderId()));
            ticket.setSalesOrder(salesOrder);
        } else {
            ticket.setSalesOrder(null);
        }

        // Update vehicle
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.getVehicleId()));
            ticket.setVehicle(vehicle);
        } else {
            ticket.setVehicle(null);
        }

        // Update other fields
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCategory(request.getCategory());
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }

        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        log.info("Ticket updated successfully: {}", ticketId);

        return convertToDetailResponse(updatedTicket);
    }

    @Override
    public SupportTicketDetailResponse assignTicket(Long ticketId, Long userId) {
        log.info("Assigning ticket {} to user {}", ticketId, userId);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ticket.setAssignedUser(user);

        // Change status to IN_PROGRESS if it was PENDING
        if ("PENDING".equals(ticket.getStatus()) || "OPEN".equals(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }

        SupportTicket assignedTicket = supportTicketRepository.save(ticket);
        log.info("Ticket assigned successfully: {}", ticketId);

        return convertToDetailResponse(assignedTicket);
    }

    @Override
    public SupportTicketDetailResponse updateTicketStatus(Long ticketId, String status) {
        log.info("Updating ticket {} status to {}", ticketId, status);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        // Validate status
        List<String> validStatuses = Arrays.asList("OPEN", "PENDING", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new BadRequestException("Invalid status: " + status);
        }

        ticket.setStatus(status);

        // Set closedAt if status is CLOSED or RESOLVED
        if ("CLOSED".equals(status) || "RESOLVED".equals(status)) {
            ticket.setClosedAt(OffsetDateTime.now());
        }

        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        log.info("Ticket status updated successfully: {}", ticketId);

        return convertToDetailResponse(updatedTicket);
    }

    @Override
    public SupportTicketDetailResponse closeTicket(Long ticketId) {
        log.info("Closing ticket: {}", ticketId);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if ("CLOSED".equals(ticket.getStatus())) {
            throw new BadRequestException("Ticket is already closed");
        }

        ticket.setStatus("CLOSED");
        ticket.setClosedAt(OffsetDateTime.now());

        SupportTicket closedTicket = supportTicketRepository.save(ticket);
        log.info("Ticket closed successfully: {}", ticketId);

        return convertToDetailResponse(closedTicket);
    }

    @Override
    public SupportTicketDetailResponse reopenTicket(Long ticketId) {
        log.info("Reopening ticket: {}", ticketId);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (!"CLOSED".equals(ticket.getStatus()) && !"RESOLVED".equals(ticket.getStatus())) {
            throw new BadRequestException("Only closed or resolved tickets can be reopened");
        }

        ticket.setStatus("OPEN");
        ticket.setClosedAt(null);

        SupportTicket reopenedTicket = supportTicketRepository.save(ticket);
        log.info("Ticket reopened successfully: {}", ticketId);

        return convertToDetailResponse(reopenedTicket);
    }

    @Override
    public void deleteTicket(Long ticketId) {
        log.info("Deleting ticket: {}", ticketId);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        supportTicketRepository.delete(ticket);
        log.info("Ticket deleted successfully: {}", ticketId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTicketStatistics() {
        log.info("Getting ticket statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", supportTicketRepository.count());
        stats.put("open", supportTicketRepository.countByStatus("OPEN"));
        stats.put("pending", supportTicketRepository.countByStatus("PENDING"));
        stats.put("inProgress", supportTicketRepository.countByStatus("IN_PROGRESS"));
        stats.put("resolved", supportTicketRepository.countByStatus("RESOLVED"));
        stats.put("closed", supportTicketRepository.countByStatus("CLOSED"));
        stats.put("cancelled", supportTicketRepository.countByStatus("CANCELLED"));

        return stats;
    }

    // ===== HELPER METHODS =====

    private Specification<SupportTicket> buildSpecification(SupportTicketFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search keyword
            if (filter.getSearchKeyword() != null && !filter.getSearchKeyword().isEmpty()) {
                String keyword = "%" + filter.getSearchKeyword().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), keyword);
                Predicate descPred = cb.like(cb.lower(root.get("description")), keyword);
                predicates.add(cb.or(titlePred, descPred));
            }

            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPriority() != null && !filter.getPriority().isEmpty()) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }

            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), filter.getCustomerId()));
            }

            if (filter.getAssignedUserId() != null) {
                predicates.add(cb.equal(root.get("assignedUser").get("id"), filter.getAssignedUserId()));
            }

            if (filter.getSalesOrderId() != null) {
                predicates.add(cb.equal(root.get("salesOrder").get("id"), filter.getSalesOrderId()));
            }

            if (filter.getVehicleId() != null && !filter.getVehicleId().isEmpty()) {
                predicates.add(cb.equal(root.get("vehicle").get("id"), filter.getVehicleId()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(SupportTicketFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? Math.min(filter.getSize(), 100) : 20;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if (filter.getSortBy() != null) {
            switch (filter.getSortBy()) {
                case "created_asc":
                    sort = Sort.by(Sort.Direction.ASC, "createdAt");
                    break;
                case "created_desc":
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
                case "title_asc":
                    sort = Sort.by(Sort.Direction.ASC, "title");
                    break;
                case "title_desc":
                    sort = Sort.by(Sort.Direction.DESC, "title");
                    break;
                case "priority_desc":
                    sort = Sort.by(Sort.Direction.DESC, "priority");
                    break;
                default:
                    break;
            }
        }

        return PageRequest.of(page, size, sort);
    }

    private SupportTicketListResponse convertToListResponse(SupportTicket t) {
        // Calculate days open
        Long daysOpen = null;
        if (t.getCreatedAt() != null) {
            OffsetDateTime endTime = t.getClosedAt() != null ? t.getClosedAt() : OffsetDateTime.now();
            daysOpen = ChronoUnit.DAYS.between(t.getCreatedAt(), endTime);
        }

        return SupportTicketListResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus())
                .priority(t.getPriority())
                .category(t.getCategory())
                .createdAt(t.getCreatedAt())
                .closedAt(t.getClosedAt())
                .daysOpen(daysOpen)
                // Customer info
                .customerId(t.getCustomer() != null ? t.getCustomer().getId() : null)
                .customerName(t.getCustomer() != null ? t.getCustomer().getFullName() : null)
                .customerPhone(t.getCustomer() != null ? t.getCustomer().getPhoneNumber() : null)
                // Assigned user info
                .assignedUserId(t.getAssignedUser() != null ? t.getAssignedUser().getId() : null)
                .assignedUserName(t.getAssignedUser() != null ? t.getAssignedUser().getFullName() : null)
                // Related info
                .salesOrderId(t.getSalesOrder() != null ? t.getSalesOrder().getId() : null)
                .vehicleId(t.getVehicle() != null ? t.getVehicle().getId() : null)
                .build();
    }

    private SupportTicketDetailResponse convertToDetailResponse(SupportTicket t) {
        // Calculate days open
        Long daysOpen = null;
        Long hoursToResolve = null;
        if (t.getCreatedAt() != null) {
            OffsetDateTime endTime = t.getClosedAt() != null ? t.getClosedAt() : OffsetDateTime.now();
            daysOpen = ChronoUnit.DAYS.between(t.getCreatedAt(), endTime);

            if (t.getClosedAt() != null) {
                hoursToResolve = ChronoUnit.HOURS.between(t.getCreatedAt(), t.getClosedAt());
            }
        }

        return SupportTicketDetailResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .category(t.getCategory())
                .createdAt(t.getCreatedAt())
                .closedAt(t.getClosedAt())
                .daysOpen(daysOpen)
                .hoursToResolve(hoursToResolve)
                // Customer info
                .customerId(t.getCustomer() != null ? t.getCustomer().getId() : null)
                .customerName(t.getCustomer() != null ? t.getCustomer().getFullName() : null)
                .customerEmail(t.getCustomer() != null ? t.getCustomer().getEmail() : null)
                .customerPhone(t.getCustomer() != null ? t.getCustomer().getPhoneNumber() : null)
                .customerAddress(t.getCustomer() != null ? t.getCustomer().getAddress() : null)
                // Assigned user info
                .assignedUserId(t.getAssignedUser() != null ? t.getAssignedUser().getId() : null)
                .assignedUserName(t.getAssignedUser() != null ? t.getAssignedUser().getFullName() : null)
                .assignedUserEmail(t.getAssignedUser() != null ? t.getAssignedUser().getEmail() : null)
                .assignedUserRole(t.getAssignedUser() != null && t.getAssignedUser().getRole() != null ?
                        t.getAssignedUser().getRole().getRoleName() : null)
                // Sales Order info
                .salesOrderId(t.getSalesOrder() != null ? t.getSalesOrder().getId() : null)
                .orderReference(t.getSalesOrder() != null ? "ORD-" + t.getSalesOrder().getId() : null)
                .orderStatus(t.getSalesOrder() != null ? t.getSalesOrder().getStatus() : null)
                // Vehicle info
                .vehicleId(t.getVehicle() != null ? t.getVehicle().getId() : null)
                .vehicleBrand(t.getVehicle() != null && t.getVehicle().getProduct() != null &&
                        t.getVehicle().getProduct().getBrand() != null ?
                        t.getVehicle().getProduct().getBrand().getBrandName() : null)
                .vehicleModel(t.getVehicle() != null && t.getVehicle().getProduct() != null ?
                        t.getVehicle().getProduct().getProductName() : null)
                .vehicleVin(t.getVehicle() != null ? t.getVehicle().getVin() : null)
                .build();
    }
}
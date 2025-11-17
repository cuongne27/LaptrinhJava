package com.evm.backend.service.impl;

import com.evm.backend.dto.request.SellInRequestFilterRequest;
import com.evm.backend.dto.request.SellInRequestRequest;
import com.evm.backend.dto.response.SellInRequestResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.SellInRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SellInRequestServiceImpl implements SellInRequestService {

    private final SellInRequestRepository sellInRequestRepository;
    private final SellInRequestDetailsRepository sellInRequestDetailsRepository;
    private final DealerRepository dealerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SellInRequestResponse createRequest(SellInRequestRequest request) {
        log.info("Creating sell-in request for dealer: {}", request.getDealerId());

        // Validate dealer
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        // Generate request number
        String requestNumber = generateRequestNumber();

        // Create request
        SellInRequest sellInRequest = SellInRequest.builder()
                .requestNumber(requestNumber)
                .requestDate(request.getRequestDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status("PENDING")
                .dealer(dealer)
                .deliveryAddress(request.getDeliveryAddress() != null ?
                        request.getDeliveryAddress() : dealer.getAddress())
                .notes(request.getNotes())
                .sellInRequestDetails(new HashSet<>())
                .build();

        SellInRequest saved = sellInRequestRepository.save(sellInRequest);

        // Add items
        for (SellInRequestRequest.SellInRequestDetailDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDto.getProductId()));

            SellInRequestDetails detail = SellInRequestDetails.builder()
                    .sellInRequest(saved)
                    .product(product)
                    .requestedQuantity(itemDto.getQuantity())
                    .approvedQuantity(0)
                    .color(itemDto.getColor())
                    .notes(itemDto.getNotes())
                    .build();

            saved.getSellInRequestDetails().add(detail);
        }

        SellInRequest updated = sellInRequestRepository.save(saved);
        log.info("Sell-in request created: {}", requestNumber);
        return convertToResponse(updated);
    }

    @Override
    public SellInRequestResponse getRequestById(Long id) {
        SellInRequest request = sellInRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));
        return convertToResponse(request);
    }

    @Override
    public SellInRequestResponse getRequestByNumber(String requestNumber) {
        SellInRequest request = sellInRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));
        return convertToResponse(request);
    }

    @Override
    public Page<SellInRequestResponse> getAllRequests(SellInRequestFilterRequest filterRequest) {
        Pageable pageable = buildPageable(filterRequest);

        Page<SellInRequest> requests = sellInRequestRepository.findRequestsWithFilters(
                filterRequest.getDealerId(),
                filterRequest.getStatus(),
                filterRequest.getFromDate(),
                filterRequest.getToDate(),
                pageable
        );

        return requests.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public SellInRequestResponse updateRequest(Long id, SellInRequestRequest request) {
        log.info("Updating sell-in request: {}", id);

        SellInRequest sellInRequest = sellInRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        if (!"PENDING".equals(sellInRequest.getStatus())) {
            throw new BadRequestException("Chỉ có thể sửa yêu cầu ở trạng thái PENDING");
        }

        // Update fields
        sellInRequest.setRequestDate(request.getRequestDate());
        sellInRequest.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        sellInRequest.setNotes(request.getNotes());

        if (request.getDeliveryAddress() != null) {
            sellInRequest.setDeliveryAddress(request.getDeliveryAddress());
        }

        // Update items - clear and re-add
        sellInRequest.getSellInRequestDetails().clear();

        for (SellInRequestRequest.SellInRequestDetailDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            SellInRequestDetails detail = SellInRequestDetails.builder()
                    .sellInRequest(sellInRequest)
                    .product(product)
                    .requestedQuantity(itemDto.getQuantity())
                    .approvedQuantity(0)
                    .color(itemDto.getColor())
                    .notes(itemDto.getNotes())
                    .build();

            sellInRequest.getSellInRequestDetails().add(detail);
        }

        SellInRequest updated = sellInRequestRepository.save(sellInRequest);
        log.info("Sell-in request updated: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRequest(Long id) {
        SellInRequest request = sellInRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Chỉ có thể xóa yêu cầu ở trạng thái PENDING");
        }

        sellInRequestRepository.delete(request);
        log.info("Sell-in request deleted: {}", id);
    }

    @Override
    @Transactional
    public SellInRequestResponse approveRequest(Long id, String approvalNotes, Long approvedBy) {
        log.info("Approving sell-in request: {}", id);

        SellInRequest request = sellInRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Chỉ có thể phê duyệt yêu cầu ở trạng thái PENDING");
        }

        User approver = null;
        if (approvedBy != null) {
            approver = userRepository.findById(approvedBy)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        request.setStatus("APPROVED");
        request.setApprovalNotes(approvalNotes);
        request.setApprovedBy(approver); // FIX: Dùng approvedBy thay vì approver

        // Set approved quantity = requested quantity by default
        for (SellInRequestDetails detail : request.getSellInRequestDetails()) {
            detail.setApprovedQuantity(detail.getRequestedQuantity());
        }

        SellInRequest updated = sellInRequestRepository.save(request);
        log.info("Sell-in request approved: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public SellInRequestResponse rejectRequest(Long id, String rejectionNotes, Long rejectedBy) {
        log.info("Rejecting sell-in request: {}", id);

        SellInRequest request = sellInRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Chỉ có thể từ chối yêu cầu ở trạng thái PENDING");
        }

        User rejector = null;
        if (rejectedBy != null) {
            rejector = userRepository.findById(rejectedBy)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        request.setStatus("REJECTED");
        request.setApprovalNotes(rejectionNotes);
        request.setApprovedBy(rejector);

        SellInRequest updated = sellInRequestRepository.save(request);
        log.info("Sell-in request rejected: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public SellInRequestResponse updateStatus(Long id, String status) {
        SellInRequest request = sellInRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        request.setStatus(status);
        SellInRequest updated = sellInRequestRepository.save(request);

        log.info("Sell-in request status updated: {} -> {}", id, status);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public SellInRequestResponse markAsInTransit(Long id) {
        SellInRequest request = sellInRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        if (!"APPROVED".equals(request.getStatus())) {
            throw new BadRequestException("Chỉ có thể chuyển sang IN_TRANSIT từ trạng thái APPROVED");
        }

        request.setStatus("IN_TRANSIT");
        SellInRequest updated = sellInRequestRepository.save(request);

        log.info("Sell-in request marked as in transit: {}", id);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public SellInRequestResponse markAsDelivered(Long id) {
        SellInRequest request = sellInRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sell-in request not found"));

        if (!"IN_TRANSIT".equals(request.getStatus())) {
            throw new BadRequestException("Chỉ có thể chuyển sang DELIVERED từ trạng thái IN_TRANSIT");
        }

        request.setStatus("DELIVERED");
        request.setActualDeliveryDate(LocalDate.now());

        // Update delivered quantity
        for (SellInRequestDetails detail : request.getSellInRequestDetails()) {
            detail.setDeliveredQuantity(detail.getApprovedQuantity());
        }

        SellInRequest updated = sellInRequestRepository.save(request);

        // TODO: Update inventory - add vehicles to dealer's stock

        log.info("Sell-in request marked as delivered: {}", id);
        return convertToResponse(updated);
    }

    @Override
    public List<SellInRequestResponse> getRequestsByDealer(Long dealerId) {
        return sellInRequestRepository.findByDealerId(dealerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellInRequestResponse> getPendingRequests() {
        return sellInRequestRepository.findByStatus("PENDING")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellInRequestResponse> getUpcomingDeliveries() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        return sellInRequestRepository.findUpcomingDeliveries(today, nextWeek)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private String generateRequestNumber() {
        String prefix = "SIR-" + LocalDate.now().getYear() + "-";
        Long count = sellInRequestRepository.count() + 1;
        return prefix + String.format("%05d", count);
    }

    private SellInRequestResponse convertToResponse(SellInRequest r) {
        LocalDate today = LocalDate.now();

        boolean canApprove = "PENDING".equals(r.getStatus());
        boolean canReject = "PENDING".equals(r.getStatus());
        boolean canCancel = "PENDING".equals(r.getStatus()) || "APPROVED".equals(r.getStatus());

        Integer daysUntilDelivery = null;
        if (r.getExpectedDeliveryDate() != null && r.getExpectedDeliveryDate().isAfter(today)) {
            daysUntilDelivery = (int) ChronoUnit.DAYS.between(today, r.getExpectedDeliveryDate());
        }

        // Calculate totals
        int totalQty = r.getSellInRequestDetails().stream()
                .mapToInt(SellInRequestDetails::getRequestedQuantity)
                .sum();

        BigDecimal totalAmount = r.getSellInRequestDetails().stream()
                .map(detail -> {
                    BigDecimal price = detail.getProduct().getMsrp() != null ?
                            detail.getProduct().getMsrp() : BigDecimal.ZERO;
                    return price.multiply(new BigDecimal(detail.getRequestedQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert items
        List<SellInRequestResponse.SellInRequestItemResponse> items =
                r.getSellInRequestDetails().stream()
                        .map(this::convertItemToResponse)
                        .collect(Collectors.toList());

        SellInRequestResponse.SellInRequestResponseBuilder builder = SellInRequestResponse.builder()
                .id(r.getId())
                .requestNumber(r.getRequestNumber())
                .requestDate(r.getRequestDate())
                .expectedDeliveryDate(r.getExpectedDeliveryDate())
                .actualDeliveryDate(r.getActualDeliveryDate())
                .status(r.getStatus())
                .deliveryAddress(r.getDeliveryAddress())
                .items(items)
                .totalQuantity(totalQty)
                .totalAmount(totalAmount)
                .notes(r.getNotes())
                .approvalNotes(r.getApprovalNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .canApprove(canApprove)
                .canReject(canReject)
                .canCancel(canCancel)
                .daysUntilExpectedDelivery(daysUntilDelivery);

        // Dealer
        if (r.getDealer() != null) {
            builder.dealerId(r.getDealer().getId())
                    .dealerName(r.getDealer().getDealerName())
                    .dealerAddress(r.getDealer().getAddress());
        }

        // Requested by
        if (r.getRequestedBy() != null) {
            builder.requestedBy(r.getRequestedBy().getId())
                    .requestedByName(r.getRequestedBy().getFullName());
        }

        // Approved by
        if (r.getApprovedBy() != null) {
            builder.approvedBy(r.getApprovedBy().getId())
                    .approvedByName(r.getApprovedBy().getFullName());
        }

        return builder.build();
    }

    private SellInRequestResponse.SellInRequestItemResponse convertItemToResponse(SellInRequestDetails detail) {
        BigDecimal unitPrice = detail.getProduct().getMsrp() != null ?
                detail.getProduct().getMsrp() : BigDecimal.ZERO;
        BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(detail.getRequestedQuantity()));

        return SellInRequestResponse.SellInRequestItemResponse.builder()
                .id(detail.getId())
                .productId(detail.getProduct().getId())
                .productName(detail.getProduct().getProductName())
                .productVersion(detail.getProduct().getVersion())
                .productImageUrl(detail.getProduct().getImageUrl())
                .requestedQuantity(detail.getRequestedQuantity())
                .approvedQuantity(detail.getApprovedQuantity())
                .deliveredQuantity(detail.getDeliveredQuantity())
                .color(detail.getColor())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .notes(detail.getNotes())
                .build();
    }

    private Pageable buildPageable(SellInRequestFilterRequest req) {
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 20;
        if (size > 100) size = 100;

        Sort sort = Sort.unsorted();
        if (req.getSortBy() != null) {
            switch (req.getSortBy()) {
                case "date_asc": sort = Sort.by(Sort.Direction.ASC, "requestDate"); break;
                case "date_desc": sort = Sort.by(Sort.Direction.DESC, "requestDate"); break;
                case "status_asc": sort = Sort.by(Sort.Direction.ASC, "status"); break;
                case "status_desc": sort = Sort.by(Sort.Direction.DESC, "status"); break;
                default: sort = Sort.by(Sort.Direction.DESC, "requestDate");
            }
        } else {
            sort = Sort.by(Sort.Direction.DESC, "requestDate");
        }

        return PageRequest.of(page, size, sort);
    }
}
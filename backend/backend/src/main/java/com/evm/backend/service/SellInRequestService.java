package com.evm.backend.service;

import com.evm.backend.dto.request.SellInRequestFilterRequest;
import com.evm.backend.dto.request.SellInRequestRequest;
import com.evm.backend.dto.response.SellInRequestResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SellInRequestService {

    SellInRequestResponse createRequest(SellInRequestRequest request);

    SellInRequestResponse getRequestById(Long id);

    SellInRequestResponse getRequestByNumber(String requestNumber);

    Page<SellInRequestResponse> getAllRequests(SellInRequestFilterRequest filterRequest);

    SellInRequestResponse updateRequest(Long id, SellInRequestRequest request);

    void deleteRequest(Long id);

    SellInRequestResponse approveRequest(Long id, String approvalNotes, Long approvedBy);

    SellInRequestResponse rejectRequest(Long id, String rejectionNotes, Long rejectedBy);

    SellInRequestResponse updateStatus(Long id, String status);

    SellInRequestResponse markAsInTransit(Long id);

    SellInRequestResponse markAsDelivered(Long id);

    List<SellInRequestResponse> getRequestsByDealer(Long dealerId);

    List<SellInRequestResponse> getPendingRequests();

    List<SellInRequestResponse> getUpcomingDeliveries();
}
package com.evm.backend.service;

import com.evm.backend.dto.request.QuotationRequest;
import com.evm.backend.dto.response.QuotationResponse;
import com.evm.backend.dto.response.SalesOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface QuotationService {

    QuotationResponse createQuotation(QuotationRequest request);

    QuotationResponse getQuotationById(Long id);

    QuotationResponse getQuotationByNumber(String quotationNumber);

    Page<QuotationResponse> getAllQuotations(Pageable pageable);

    QuotationResponse updateQuotation(Long id, QuotationRequest request);

    void deleteQuotation(Long id);

    QuotationResponse sendQuotation(Long id); // Gửi báo giá cho khách

    QuotationResponse acceptQuotation(Long id); // Khách chấp nhận

    QuotationResponse rejectQuotation(Long id); // Khách từ chối

    SalesOrderResponse convertToOrder(Long id);

    byte[] exportQuotationToPdf(Long id); // Xuất PDF

    List<QuotationResponse> getQuotationsByCustomer(Long customerId);

    List<QuotationResponse> getQuotationsBySalesPerson(Long salesPersonId);

    List<QuotationResponse> getExpiredQuotations();

    void autoExpireQuotations(); // Cron job tự động hết hạn

    QuotationResponse recalculateQuotation(Long id); // Tính lại giá
}
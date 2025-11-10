package com.evm.backend.controller;

import com.evm.backend.dto.request.DealerContractFilterRequest;
import com.evm.backend.dto.request.DealerContractRequest;
import com.evm.backend.dto.response.DealerContractDetailResponse;
import com.evm.backend.dto.response.DealerContractListResponse;
import com.evm.backend.service.DealerContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for DealerContract Management
 * CRUD operations for DealerContract entity
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Contract Management", description = "APIs quản lý hợp đồng đại lý")
public class DealerContractCrudController {

    private final DealerContractService contractService;

    /**
     * GET: Lấy danh sách contracts với filter
     * Endpoint: GET /api/contracts
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy danh sách hợp đồng",
            description = "Lấy danh sách hợp đồng đại lý với filter và phân trang"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<DealerContractListResponse>> getAllContracts(
            @Parameter(description = "Brand ID để filter")
            @RequestParam(required = false) Integer brandId,

            @Parameter(description = "Dealer ID để filter")
            @RequestParam(required = false) Long dealerId,

            @Parameter(description = "Ngày bắt đầu (từ ngày này trở đi)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (đến trước ngày này)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Trạng thái: ACTIVE, EXPIRED, UPCOMING")
            @RequestParam(required = false) String status,

            @Parameter(description = "Sắp xếp: start_date_asc, start_date_desc, end_date_asc, end_date_desc")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Số lượng items mỗi trang (max 100)")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/contracts - brandId: {}, dealerId: {}, status: {}",
                brandId, dealerId, status);

        DealerContractFilterRequest filterRequest = DealerContractFilterRequest.builder()
                .brandId(brandId)
                .dealerId(dealerId)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<DealerContractListResponse> contracts = contractService.getAllContracts(filterRequest);

        return ResponseEntity.ok(contracts);
    }

    /**
     * GET: Lấy contracts của dealer
     * Endpoint: GET /api/contracts/dealer/{dealerId}
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Lấy hợp đồng theo dealer",
            description = "Lấy tất cả hợp đồng của một đại lý"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dealer"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<DealerContractListResponse>> getContractsByDealer(
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId
    ) {
        log.info("GET /api/contracts/dealer/{}", dealerId);

        List<DealerContractListResponse> contracts = contractService.getContractsByDealer(dealerId);

        return ResponseEntity.ok(contracts);
    }

    /**
     * GET: Lấy contracts của brand
     * Endpoint: GET /api/contracts/brand/{brandId}
     */
    @GetMapping("/brand/{brandId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy hợp đồng theo brand",
            description = "Lấy tất cả hợp đồng của một thương hiệu"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy brand"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<DealerContractListResponse>> getContractsByBrand(
            @Parameter(description = "Brand ID", required = true)
            @PathVariable Integer brandId
    ) {
        log.info("GET /api/contracts/brand/{}", brandId);

        List<DealerContractListResponse> contracts = contractService.getContractsByBrand(brandId);

        return ResponseEntity.ok(contracts);
    }

    /**
     * GET: Xem chi tiết contract
     * Endpoint: GET /api/contracts/{contractId}
     */
    @GetMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Xem chi tiết hợp đồng",
            description = "Xem thông tin chi tiết của hợp đồng"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hợp đồng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<DealerContractDetailResponse> getContractById(
            @Parameter(description = "Contract ID", required = true)
            @PathVariable Long contractId
    ) {
        log.info("GET /api/contracts/{}", contractId);

        DealerContractDetailResponse contract = contractService.getContractById(contractId);

        return ResponseEntity.ok(contract);
    }

    /**
     * POST: Tạo contract mới
     * Endpoint: POST /api/contracts/create
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tạo hợp đồng mới",
            description = "Tạo hợp đồng đại lý mới"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo hợp đồng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc hợp đồng overlap"),
            @ApiResponse(responseCode = "404", description = "Brand hoặc Dealer không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<DealerContractDetailResponse> createContract(
            Authentication authentication,
            @Parameter(description = "Thông tin hợp đồng cần tạo", required = true)
            @Valid @RequestBody DealerContractRequest request
    ) {
        log.info("POST /api/contracts - Dealer: {}, Brand: {}",
                request.getDealerId(), request.getBrandId());

        DealerContractDetailResponse createdContract = contractService.createContract(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
    }

    /**
     * PUT: Cập nhật contract
     * Endpoint: PUT /api/contracts/update/{contractId}
     */
    @PutMapping("/update/{contractId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Cập nhật hợp đồng",
            description = "Cập nhật thông tin hợp đồng"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hợp đồng, brand hoặc dealer"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<DealerContractDetailResponse> updateContract(
            Authentication authentication,
            @Parameter(description = "Contract ID", required = true)
            @PathVariable Long contractId,

            @Parameter(description = "Thông tin hợp đồng cần cập nhật", required = true)
            @Valid @RequestBody DealerContractRequest request
    ) {
        log.info("PUT /api/contracts/{}", contractId);

        DealerContractDetailResponse updatedContract = contractService.updateContract(contractId, request);

        return ResponseEntity.ok(updatedContract);
    }

    /**
     * DELETE: Xóa contract
     * Endpoint: DELETE /api/contracts/{contractId}
     */
    @DeleteMapping("/{contractId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa hợp đồng",
            description = "Xóa hợp đồng (chỉ ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hợp đồng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<Void> deleteContract(
            Authentication authentication,
            @Parameter(description = "Contract ID", required = true)
            @PathVariable Long contractId
    ) {
        log.info("DELETE /api/contracts/{}", contractId);

        contractService.deleteContract(contractId);

        return ResponseEntity.noContent().build();
    }
}
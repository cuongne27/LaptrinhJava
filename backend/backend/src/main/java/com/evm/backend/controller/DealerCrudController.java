package com.evm.backend.controller;

import com.evm.backend.dto.request.DealerFilterRequest;
import com.evm.backend.dto.request.DealerRequest;
import com.evm.backend.dto.response.DealerDetailResponse;
import com.evm.backend.dto.response.DealerListResponse;
import com.evm.backend.service.DealerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Dealer Management
 * CRUD operations for Dealer entity
 */
@RestController
@RequestMapping("/api/dealers") // <<< MODULE: QUẢN LÝ ĐẠI LÝ (DEALER MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Management", description = "APIs quản lý đại lý")
public class DealerCrudController {

    private final DealerService dealerService;

    // <<< CHỨC NĂNG: LẤY DANH SÁCH ĐẠI LÝ (CÓ FILTER VÀ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/dealers/filter
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy danh sách đại lý",
            description = "Lấy danh sách đại lý với filter và phân trang"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<DealerListResponse>> getAllDealers(
            @Parameter(description = "Brand ID để filter")
            @RequestParam(required = false) Integer brandId,

            @Parameter(description = "Từ khóa tìm kiếm (tên, địa chỉ, email)")
            @RequestParam(required = false) String searchKeyword,

            @Parameter(description = "Cấp độ đại lý (Gold, Silver, Bronze, Platinum)")
            @RequestParam(required = false) String dealerLevel,

            @Parameter(description = "Sắp xếp: name_asc, name_desc, level_asc, level_desc")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Số lượng items mỗi trang (max 100)")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/dealers - brandId: {}, search: {}, level: {}",
                brandId, searchKeyword, dealerLevel);

        DealerFilterRequest filterRequest = DealerFilterRequest.builder()
                .brandId(brandId)
                .searchKeyword(searchKeyword)
                .dealerLevel(dealerLevel)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<DealerListResponse> dealers = dealerService.getAllDealers(filterRequest);

        return ResponseEntity.ok(dealers);
    }

    // <<< CHỨC NĂNG: LẤY DANH SÁCH ĐẠI LÝ THEO BRAND (KHÔNG PHÂN TRANG)
    // <<< ĐẦU API: GET /api/dealers/brand/{brandId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN, DEALER_STAFF
    @GetMapping("/brand/{brandId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Lấy danh sách đại lý theo brand",
            description = "Lấy tất cả đại lý của một thương hiệu (không phân trang)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy brand"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<DealerListResponse>> getDealersByBrand(
            @Parameter(description = "Brand ID", required = true)
            @PathVariable Integer brandId
    ) {
        log.info("GET /api/dealers/brand/{}", brandId);

        List<DealerListResponse> dealers = dealerService.getDealersByBrand(brandId);

        return ResponseEntity.ok(dealers);
    }

    // <<< CHỨC NĂNG: XEM CHI TIẾT ĐẠI LÝ
    // <<< ĐẦU API: GET /api/dealers/{dealerId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN, DEALER_STAFF
    @GetMapping("/{dealerId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Xem chi tiết đại lý",
            description = "Xem thông tin chi tiết của đại lý bao gồm statistics"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đại lý"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<DealerDetailResponse> getDealerById(
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId
    ) {
        log.info("GET /api/dealers/{}", dealerId);

        DealerDetailResponse dealer = dealerService.getDealerById(dealerId);

        return ResponseEntity.ok(dealer);
    }

    // <<< CHỨC NĂNG: TẠO ĐẠI LÝ MỚI
    // <<< ĐẦU API: POST /api/dealers/create
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tạo đại lý mới",
            description = "Tạo một đại lý mới"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đại lý thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email đã tồn tại"),
            @ApiResponse(responseCode = "404", description = "Brand không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<DealerDetailResponse> createDealer(
            Authentication authentication,
            @Parameter(description = "Thông tin đại lý cần tạo", required = true)
            @Valid @RequestBody DealerRequest request
    ) {
        log.info("POST /api/dealers - Dealer: {}", request.getDealerName());

        DealerDetailResponse createdDealer = dealerService.createDealer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDealer);
    }

    // <<< CHỨC NĂNG: CẬP NHẬT ĐẠI LÝ
    // <<< ĐẦU API: PUT /api/dealers/update/{dealerId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @PutMapping("/update/{dealerId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Cập nhật đại lý",
            description = "Cập nhật thông tin đại lý"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đại lý hoặc brand"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<DealerDetailResponse> updateDealer(
            Authentication authentication,
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId,

            @Parameter(description = "Thông tin đại lý cần cập nhật", required = true)
            @Valid @RequestBody DealerRequest request
    ) {
        log.info("PUT /api/dealers/{} - Dealer: {}", dealerId, request.getDealerName());

        DealerDetailResponse updatedDealer = dealerService.updateDealer(dealerId, request);

        return ResponseEntity.ok(updatedDealer);
    }

    // <<< CHỨC NĂNG: XÓA ĐẠI LÝ (ADMIN ONLY)
    // <<< ĐẦU API: DELETE /api/dealers/delete/{dealerId}
    // <<< VAI TRÒ: ADMIN
    @DeleteMapping("/delete/{dealerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa đại lý",
            description = "Xóa đại lý (chỉ ADMIN). Không thể xóa nếu còn users/vehicles/appointments liên quan"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa vì còn dữ liệu liên quan"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đại lý"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<Void> deleteDealer(
            Authentication authentication,
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId
    ) {
        log.info("DELETE /api/dealers/{}", dealerId);

        dealerService.deleteDealer(dealerId);

        return ResponseEntity.noContent().build();
    }
}
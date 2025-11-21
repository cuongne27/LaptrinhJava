package com.evm.backend.controller;

import com.evm.backend.dto.request.PromotionFilterRequest;
import com.evm.backend.dto.request.PromotionRequest;
import com.evm.backend.dto.response.PromotionDetailResponse;
import com.evm.backend.dto.response.PromotionListResponse;
import com.evm.backend.service.PromotionService;
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
 * CRUD operations for Promotion entity
 */
@RestController
@RequestMapping("/api/promotions") // <<< MODULE: QUẢN LÝ CHƯƠNG TRÌNH KHUYẾN MÃI (PROMOTION MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "APIs quản lý chương trình khuyến mãi")
public class PromotionCrudController {

    private final PromotionService promotionService;

    /**
     * GET: Lấy danh sách promotions với filter
     * Endpoint: GET /api/promotions
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH KHUYẾN MÃI (CÓ LỌC VÀ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/promotions
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy danh sách chương trình khuyến mãi",
            description = "Lấy danh sách chương trình khuyến mãi với filter và phân trang"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<PromotionListResponse>> getAllPromotions(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã khuyến mãi)")
            @RequestParam(required = false) String searchKeyword,

            @Parameter(description = "Trạng thái (Active, Inactive, Expired)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Sắp xếp: name_asc, name_desc, start_date_asc, start_date_desc, end_date_asc, end_date_desc")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Số lượng items mỗi trang (max 100)")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/promotions - search: {}, status: {}", searchKeyword, status);

        PromotionFilterRequest filterRequest = PromotionFilterRequest.builder()
                .searchKeyword(searchKeyword)
                .status(status)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<PromotionListResponse> promotions = promotionService.getAllPromotions(filterRequest);

        return ResponseEntity.ok(promotions);
    }

    /**
     * GET: Lấy danh sách promotions đang hoạt động
     * Endpoint: GET /api/promotions/active
     */
    // <<< CHỨC NĂNG: LẤY DANH SÁCH KHUYẾN MÃI ĐANG HOẠT ĐỘNG
    // <<< ĐẦU API: GET /api/promotions/active
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy danh sách chương trình khuyến mãi đang hoạt động",
            description = "Lấy danh sách các chương trình khuyến mãi đang còn hiệu lực"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<PromotionListResponse>> getActivePromotions() {
        log.info("GET /api/promotions/active");

        List<PromotionListResponse> activePromotions = promotionService.getActivePromotions();

        return ResponseEntity.ok(activePromotions);
    }

    /**
     * GET: Xem chi tiết promotion
     * Endpoint: GET /api/promotions/{promotionId}
     */
    // <<< CHỨC NĂNG: XEM CHI TIẾT CHƯƠNG TRÌNH KHUYẾN MÃI THEO ID
    // <<< ĐẦU API: GET /api/promotions/{promotionId}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/{promotionId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Xem chi tiết chương trình khuyến mãi",
            description = "Xem thông tin chi tiết của chương trình khuyến mãi"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<PromotionDetailResponse> getPromotionById(
            @Parameter(description = "Promotion ID", required = true)
            @PathVariable Long promotionId
    ) {
        log.info("GET /api/promotions/{}", promotionId);

        PromotionDetailResponse promotion = promotionService.getPromotionById(promotionId);

        return ResponseEntity.ok(promotion);
    }

    /**
     * GET: Tìm promotion theo code
     * Endpoint: GET /api/promotions/code/{promotionCode}
     */
    // <<< CHỨC NĂNG: TÌM CHI TIẾT CHƯƠNG TRÌNH KHUYẾN MÃI THEO MÃ (CODE)
    // <<< ĐẦU API: GET /api/promotions/code/{promotionCode}
    // <<< VAI TRÒ: DEALER_STAFF, BRAND_MANAGER, ADMIN
    @GetMapping("/code/{promotionCode}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tìm chương trình khuyến mãi theo mã",
            description = "Tìm chương trình khuyến mãi theo mã khuyến mãi"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<PromotionDetailResponse> getPromotionByCode(
            @Parameter(description = "Promotion Code", required = true)
            @PathVariable String promotionCode
    ) {
        log.info("GET /api/promotions/code/{}", promotionCode);

        PromotionDetailResponse promotion = promotionService.getPromotionByCode(promotionCode);

        return ResponseEntity.ok(promotion);
    }

    /**
     * POST: Tạo promotion mới
     * Endpoint: POST /api/promotions
     */
    // <<< CHỨC NĂNG: TẠO CHƯƠNG TRÌNH KHUYẾN MÃI MỚI
    // <<< ĐẦU API: POST /api/promotions
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tạo chương trình khuyến mãi mới",
            description = "Tạo một chương trình khuyến mãi mới trong hệ thống"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo chương trình khuyến mãi thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc mã khuyến mãi đã tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<PromotionDetailResponse> createPromotion(
            Authentication authentication,
            @Parameter(description = "Thông tin chương trình khuyến mãi cần tạo", required = true)
            @Valid @RequestBody PromotionRequest request
    ) {
        log.info("POST /api/promotions - Promotion: {}", request.getPromotionName());

        PromotionDetailResponse createdPromotion = promotionService.createPromotion(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPromotion);
    }

    /**
     * PUT: Cập nhật promotion
     * Endpoint: PUT /api/promotions/{promotionId}
     */
    // <<< CHỨC NĂNG: CẬP NHẬT CHƯƠNG TRÌNH KHUYẾN MÃI
    // <<< ĐẦU API: PUT /api/promotions/{promotionId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN
    @PutMapping("/{promotionId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Cập nhật chương trình khuyến mãi",
            description = "Cập nhật thông tin chương trình khuyến mãi"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<PromotionDetailResponse> updatePromotion(
            Authentication authentication,
            @Parameter(description = "Promotion ID", required = true)
            @PathVariable Long promotionId,

            @Parameter(description = "Thông tin chương trình khuyến mãi cần cập nhật", required = true)
            @Valid @RequestBody PromotionRequest request
    ) {
        log.info("PUT /api/promotions/{} - Promotion: {}", promotionId, request.getPromotionName());

        PromotionDetailResponse updatedPromotion = promotionService.updatePromotion(promotionId, request);

        return ResponseEntity.ok(updatedPromotion);
    }

    /**
     * DELETE: Xóa promotion
     * Endpoint: DELETE /api/promotions/{promotionId}
     */
    // <<< CHỨC NĂNG: XÓA VĨNH VIỄN CHƯƠNG TRÌNH KHUYẾN MÃI
    // <<< ĐẦU API: DELETE /api/promotions/{promotionId}
    // <<< VAI TRÒ: ADMIN (Chỉ ADMIN mới được xóa vĩnh viễn)
    @DeleteMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa chương trình khuyến mãi",
            description = "Xóa chương trình khuyến mãi (chỉ ADMIN). Không thể xóa nếu còn orders liên quan"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa vì còn dữ liệu liên quan"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<Void> deletePromotion(
            Authentication authentication,
            @Parameter(description = "Promotion ID", required = true)
            @PathVariable Long promotionId
    ) {
        log.info("DELETE /api/promotions/{}", promotionId);

        promotionService.deletePromotion(promotionId);

        return ResponseEntity.noContent().build();
    }
}
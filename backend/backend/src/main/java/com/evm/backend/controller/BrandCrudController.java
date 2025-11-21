package com.evm.backend.controller;

import com.evm.backend.dto.request.BrandRequest;
import com.evm.backend.dto.response.BrandDetailResponse;
import com.evm.backend.dto.response.BrandListResponse;
import com.evm.backend.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Brand Management
 * CRUD operations for Brand entity
 */
@RestController
@RequestMapping("/api/brands") // <<< MODULE: QUẢN LÝ THƯƠNG HIỆU (BRAND MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Brand Management", description = "APIs quản lý thương hiệu xe điện")
public class BrandCrudController {

    private final BrandService brandService;

    // <<< CHỨC NĂNG: LẤY DANH SÁCH TẤT CẢ THƯƠNG HIỆU (CÓ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/brands
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN, DEALER_STAFF
    @GetMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Lấy danh sách thương hiệu",
            description = "Lấy danh sách tất cả các thương hiệu với phân trang và statistics"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<BrandListResponse>> getAllBrands(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Số lượng items mỗi trang")
            @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Sắp xếp theo (brandName, id)")
            @RequestParam(defaultValue = "brandName") String sortBy,

            @Parameter(description = "Chiều sắp xếp (asc, desc)")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("GET /api/brands - page: {}, size: {}", page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<BrandListResponse> brands = brandService.getAllBrands(pageable);

        return ResponseEntity.ok(brands);
    }

    // <<< CHỨC NĂNG: LẤY TẤT CẢ THƯƠNG HIỆU (KHÔNG PHÂN TRANG)
    // <<< ĐẦU API: GET /api/brands/all
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN, DEALER_STAFF
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Lấy tất cả thương hiệu (không phân trang)",
            description = "Lấy danh sách tất cả thương hiệu để dùng cho dropdown/select"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<BrandListResponse>> getAllBrandsWithoutPagination() {
        log.info("GET /api/brands/all");

        List<BrandListResponse> brands = brandService.getAllBrands();

        return ResponseEntity.ok(brands);
    }

    // <<< CHỨC NĂNG: XEM CHI TIẾT THƯƠNG HIỆU
    // <<< ĐẦU API: GET /api/brands/{brandId}
    // <<< VAI TRÒ: BRAND_MANAGER, ADMIN, DEALER_STAFF
    @GetMapping("/{brandId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'DEALER_STAFF')")
    @Operation(
            summary = "Xem chi tiết thương hiệu",
            description = "Xem thông tin chi tiết của thương hiệu bao gồm statistics"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<BrandDetailResponse> getBrandById(
            @Parameter(description = "Brand ID", required = true)
            @PathVariable Integer brandId
    ) {
        log.info("GET /api/brands/{}", brandId);

        BrandDetailResponse brand = brandService.getBrandById(brandId);

        return ResponseEntity.ok(brand);
    }

    // <<< CHỨC NĂNG: TẠO THƯƠNG HIỆU MỚI
    // <<< ĐẦU API: POST /api/brands/create
    // <<< VAI TRÒ: ADMIN
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Tạo thương hiệu mới",
            description = "Tạo một thương hiệu mới (chỉ ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo thương hiệu thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc tên/mã số thuế đã tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<BrandDetailResponse> createBrand(
            Authentication authentication,
            @Parameter(description = "Thông tin thương hiệu cần tạo", required = true)
            @Valid @RequestBody BrandRequest request
    ) {
        log.info("POST /api/brands - Brand: {}", request.getBrandName());

        BrandDetailResponse createdBrand = brandService.createBrand(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdBrand);
    }

    // <<< CHỨC NĂNG: CẬP NHẬT THƯƠNG HIỆU
    // <<< ĐẦU API: PUT /api/brands/update/{brandId}
    // <<< VAI TRÒ: ADMIN
    @PutMapping("/update/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cập nhật thương hiệu",
            description = "Cập nhật thông tin thương hiệu (chỉ ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<BrandDetailResponse> updateBrand(
            Authentication authentication,
            @Parameter(description = "Brand ID", required = true)
            @PathVariable Integer brandId,

            @Parameter(description = "Thông tin thương hiệu cần cập nhật", required = true)
            @Valid @RequestBody BrandRequest request
    ) {
        log.info("PUT /api/brands/{} - Brand: {}", brandId, request.getBrandName());

        BrandDetailResponse updatedBrand = brandService.updateBrand(brandId, request);

        return ResponseEntity.ok(updatedBrand);
    }

    // <<< CHỨC NĂNG: XÓA THƯƠNG HIỆU
    // <<< ĐẦU API: DELETE /api/brands/delete/{brandId}
    // <<< VAI TRÒ: ADMIN
    @DeleteMapping("/delete/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa thương hiệu",
            description = "Xóa thương hiệu (chỉ ADMIN). Không thể xóa nếu còn dealers/products/users liên quan"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa vì còn dữ liệu liên quan"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<Void> deleteBrand(
            Authentication authentication,
            @Parameter(description = "Brand ID", required = true)
            @PathVariable Integer brandId
    ) {
        log.info("DELETE /api/brands/{}", brandId);

        brandService.deleteBrand(brandId);

        return ResponseEntity.noContent().build();
    }
}
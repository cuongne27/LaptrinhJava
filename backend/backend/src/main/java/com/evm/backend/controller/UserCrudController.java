package com.evm.backend.controller;

import com.evm.backend.dto.request.UserFilterRequest;
import com.evm.backend.dto.request.UserRequest;
import com.evm.backend.dto.response.UserDetailResponse;
import com.evm.backend.dto.response.UserListResponse;
import com.evm.backend.service.UserService;
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
 * CRUD operations for User entity (Staff/Employee Management)
 * Only ADMIN can perform these operations
 */
@RestController
@RequestMapping("/api/users") // <<< MODULE: QUẢN LÝ NHÂN VIÊN (STAFF/EMPLOYEE MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs quản lý nhân viên (chỉ ADMIN)")
public class UserCrudController {

    private final UserService userService;

    // <<< CHỨC NĂNG: LẤY DANH SÁCH NHÂN VIÊN (CÓ FILTER VÀ PHÂN TRANG)
    // <<< ĐẦU API: GET /api/users
    // <<< VAI TRÒ: ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Lấy danh sách nhân viên",
            description = "Lấy danh sách nhân viên với filter và phân trang (chỉ ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<Page<UserListResponse>> getAllUsers(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String searchKeyword,
            @Parameter(description = "Tên role") @RequestParam(required = false) String roleName,
            @Parameter(description = "Role ID") @RequestParam(required = false) Long roleId,
            @Parameter(description = "Brand ID") @RequestParam(required = false) Integer brandId,
            @Parameter(description = "Dealer ID") @RequestParam(required = false) Long dealerId,
            @Parameter(description = "Trạng thái active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Sắp xếp") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng") @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/users - keyword: {}, role: {}", searchKeyword, roleName);

        UserFilterRequest filterRequest = UserFilterRequest.builder()
                .searchKeyword(searchKeyword)
                .roleName(roleName)
                .roleId(roleId)
                .brandId(brandId)
                .dealerId(dealerId)
                .isActive(isActive)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<UserListResponse> users = userService.getAllUsers(filterRequest);
        return ResponseEntity.ok(users);
    }

    // <<< CHỨC NĂNG: LẤY NHÂN VIÊN THEO ROLE
    // <<< ĐẦU API: GET /api/users/role/{roleName}
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách nhân viên theo role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<List<UserListResponse>> getUsersByRole(
            @Parameter(description = "Role name", required = true) @PathVariable String roleName
    ) {
        log.info("GET /api/users/role/{}", roleName);
        List<UserListResponse> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    // <<< CHỨC NĂNG: LẤY NHÂN VIÊN THEO BRAND
    // <<< ĐẦU API: GET /api/users/brand/{brandId}
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/brand/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách nhân viên theo brand")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy brand")
    })
    public ResponseEntity<List<UserListResponse>> getUsersByBrand(
            @Parameter(description = "Brand ID", required = true) @PathVariable Integer brandId
    ) {
        log.info("GET /api/users/brand/{}", brandId);
        List<UserListResponse> users = userService.getUsersByBrand(brandId);
        return ResponseEntity.ok(users);
    }

    // <<< CHỨC NĂNG: LẤY NHÂN VIÊN THEO DEALER
    // <<< ĐẦU API: GET /api/users/dealer/{dealerId}
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách nhân viên theo dealer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dealer")
    })
    public ResponseEntity<List<UserListResponse>> getUsersByDealer(
            @Parameter(description = "Dealer ID", required = true) @PathVariable Long dealerId
    ) {
        log.info("GET /api/users/dealer/{}", dealerId);
        List<UserListResponse> users = userService.getUsersByDealer(dealerId);
        return ResponseEntity.ok(users);
    }

    // <<< CHỨC NĂNG: LẤY NHÂN VIÊN ĐANG HOẠT ĐỘNG
    // <<< ĐẦU API: GET /api/users/active
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách nhân viên đang hoạt động")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<UserListResponse>> getActiveUsers() {
        log.info("GET /api/users/active");
        List<UserListResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    // <<< CHỨC NĂNG: LẤY NHÂN VIÊN ĐÃ NGƯNG HOẠT ĐỘNG
    // <<< ĐẦU API: GET /api/users/inactive
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách nhân viên đã ngưng hoạt động")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<UserListResponse>> getInactiveUsers() {
        log.info("GET /api/users/inactive");
        List<UserListResponse> users = userService.getInactiveUsers();
        return ResponseEntity.ok(users);
    }

    // <<< CHỨC NĂNG: XEM CHI TIẾT NHÂN VIÊN
    // <<< ĐẦU API: GET /api/users/{userId}
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem chi tiết nhân viên")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<UserDetailResponse> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId
    ) {
        log.info("GET /api/users/{}", userId);
        UserDetailResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // <<< CHỨC NĂNG: TÌM NHÂN VIÊN THEO USERNAME
    // <<< ĐẦU API: GET /api/users/username/{username}
    // <<< VAI TRÒ: ADMIN
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tìm nhân viên theo username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy nhân viên"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<UserDetailResponse> getUserByUsername(
            @Parameter(description = "Username", required = true) @PathVariable String username
    ) {
        log.info("GET /api/users/username/{}", username);
        UserDetailResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    // <<< CHỨC NĂNG: TẠO TÀI KHOẢN NHÂN VIÊN MỚI
    // <<< ĐẦU API: POST /api/users
    // <<< VAI TRÒ: ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Tạo tài khoản nhân viên mới",
            description = "ADMIN tạo tài khoản cho nhân viên mới"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo tài khoản thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc username/email đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<UserDetailResponse> createUser(
            Authentication authentication,
            @Parameter(description = "Thông tin nhân viên", required = true)
            @Valid @RequestBody UserRequest request
    ) {
        log.info("POST /api/users - Username: {}", request.getUsername());
        UserDetailResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // <<< CHỨC NĂNG: CẬP NHẬT THÔNG TIN NHÂN VIÊN
    // <<< ĐẦU API: PUT /api/users/{userId}
    // <<< VAI TRÒ: ADMIN
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin nhân viên")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<UserDetailResponse> updateUser(
            Authentication authentication,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UserRequest request
    ) {
        log.info("PUT /api/users/{}", userId);
        UserDetailResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    // <<< CHỨC NĂNG: KÍCH HOẠT TÀI KHOẢN
    // <<< ĐẦU API: PATCH /api/users/{userId}/activate
    // <<< VAI TRÒ: ADMIN
    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kích hoạt tài khoản nhân viên")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kích hoạt thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<UserDetailResponse> activateUser(
            Authentication authentication,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId
    ) {
        log.info("PATCH /api/users/{}/activate", userId);
        UserDetailResponse activatedUser = userService.activateUser(userId);
        return ResponseEntity.ok(activatedUser);
    }

    // <<< CHỨC NĂNG: VÔ HIỆU HÓA TÀI KHOẢN
    // <<< ĐẦU API: PATCH /api/users/{userId}/deactivate
    // <<< VAI TRÒ: ADMIN
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vô hiệu hóa tài khoản nhân viên")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vô hiệu hóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<UserDetailResponse> deactivateUser(
            Authentication authentication,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId
    ) {
        log.info("PATCH /api/users/{}/deactivate", userId);
        UserDetailResponse deactivatedUser = userService.deactivateUser(userId);
        return ResponseEntity.ok(deactivatedUser);
    }

    // <<< CHỨC NĂNG: RESET MẬT KHẨU
    // <<< ĐẦU API: PATCH /api/users/{userId}/reset-password?newPassword={newPassword}
    // <<< VAI TRÒ: ADMIN
    @PatchMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset mật khẩu nhân viên")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset mật khẩu thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<UserDetailResponse> resetPassword(
            Authentication authentication,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Mật khẩu mới", required = true) @RequestParam String newPassword
    ) {
        log.info("PATCH /api/users/{}/reset-password", userId);
        UserDetailResponse user = userService.resetPassword(userId, newPassword);
        return ResponseEntity.ok(user);
    }

    // <<< CHỨC NĂNG: XÓA NHÂN VIÊN (SOFT DELETE)
    // <<< ĐẦU API: DELETE /api/users/{userId}
    // <<< VAI TRÒ: ADMIN
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa nhân viên (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<Void> deleteUser(
            Authentication authentication,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId
    ) {
        log.info("DELETE /api/users/{}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
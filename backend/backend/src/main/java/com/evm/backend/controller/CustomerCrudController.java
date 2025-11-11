package com.evm.backend.controller;

import com.evm.backend.dto.request.CustomerFilterRequest;
import com.evm.backend.dto.request.CustomerRequest;
import com.evm.backend.dto.response.CustomerDetailResponse;
import com.evm.backend.dto.response.CustomerListResponse;
import com.evm.backend.service.CustomerService;
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

/**
 * CRUD operations for Customer entity
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs quản lý khách hàng")
public class CustomerCrudController {

    private final CustomerService customerService;

    /**
     * GET: Lấy danh sách customers với filter
     * Endpoint: GET /api/customers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Lấy danh sách khách hàng",
            description = "Lấy danh sách khách hàng với filter và phân trang"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Page<CustomerListResponse>> getAllCustomers(
            @Parameter(description = "Từ khóa tìm kiếm (tên, sđt, email, địa chỉ)")
            @RequestParam(required = false) String searchKeyword,

            @Parameter(description = "Loại khách hàng (Individual, Corporate, VIP, Regular)")
            @RequestParam(required = false) String customerType,

            @Parameter(description = "Sắp xếp: name_asc, name_desc, created_asc, created_desc")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Số lượng items mỗi trang (max 100)")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.info("GET /api/customers - search: {}, type: {}", searchKeyword, customerType);

        CustomerFilterRequest filterRequest = CustomerFilterRequest.builder()
                .searchKeyword(searchKeyword)
                .customerType(customerType)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        Page<CustomerListResponse> customers = customerService.getAllCustomers(filterRequest);

        return ResponseEntity.ok(customers);
    }

    /**
     * GET: Xem chi tiết customer
     * Endpoint: GET /api/customers/{customerId}
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Xem chi tiết khách hàng",
            description = "Xem thông tin chi tiết của khách hàng bao gồm statistics"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<CustomerDetailResponse> getCustomerById(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId
    ) {
        log.info("GET /api/customers/{}", customerId);

        CustomerDetailResponse customer = customerService.getCustomerById(customerId);

        return ResponseEntity.ok(customer);
    }

    /**
     * GET: Tìm customer theo email
     * Endpoint: GET /api/customers/email/{email}
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tìm khách hàng theo email",
            description = "Tìm khách hàng theo địa chỉ email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy khách hàng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<CustomerDetailResponse> getCustomerByEmail(
            @Parameter(description = "Email", required = true)
            @PathVariable String email
    ) {
        log.info("GET /api/customers/email/{}", email);

        CustomerDetailResponse customer = customerService.getCustomerByEmail(email);

        return ResponseEntity.ok(customer);
    }

    /**
     * GET: Tìm customer theo phone number
     * Endpoint: GET /api/customers/phone/{phoneNumber}
     */
    @GetMapping("/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tìm khách hàng theo số điện thoại",
            description = "Tìm khách hàng theo số điện thoại"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy khách hàng"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<CustomerDetailResponse> getCustomerByPhoneNumber(
            @Parameter(description = "Phone Number", required = true)
            @PathVariable String phoneNumber
    ) {
        log.info("GET /api/customers/phone/{}", phoneNumber);

        CustomerDetailResponse customer = customerService.getCustomerByPhoneNumber(phoneNumber);

        return ResponseEntity.ok(customer);
    }

    /**
     * POST: Tạo customer mới
     * Endpoint: POST /api/customers
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Tạo khách hàng mới",
            description = "Tạo một khách hàng mới trong hệ thống"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo khách hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email/sđt đã tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<CustomerDetailResponse> createCustomer(
            Authentication authentication,
            @Parameter(description = "Thông tin khách hàng cần tạo", required = true)
            @Valid @RequestBody CustomerRequest request
    ) {
        log.info("POST /api/customers - Customer: {}", request.getFullName());

        CustomerDetailResponse createdCustomer = customerService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    /**
     * PUT: Cập nhật customer
     * Endpoint: PUT /api/customers/{customerId}
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('DEALER_STAFF', 'BRAND_MANAGER', 'ADMIN')")
    @Operation(
            summary = "Cập nhật khách hàng",
            description = "Cập nhật thông tin khách hàng"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    public ResponseEntity<CustomerDetailResponse> updateCustomer(
            Authentication authentication,
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId,

            @Parameter(description = "Thông tin khách hàng cần cập nhật", required = true)
            @Valid @RequestBody CustomerRequest request
    ) {
        log.info("PUT /api/customers/{} - Customer: {}", customerId, request.getFullName());

        CustomerDetailResponse updatedCustomer = customerService.updateCustomer(customerId, request);

        return ResponseEntity.ok(updatedCustomer);
    }

    /**
     * DELETE: Xóa customer
     * Endpoint: DELETE /api/customers/{customerId}
     */
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xóa khách hàng",
            description = "Xóa khách hàng (chỉ ADMIN). Không thể xóa nếu còn orders/tickets liên quan"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa vì còn dữ liệu liên quan"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới có quyền")
    })
    public ResponseEntity<Void> deleteCustomer(
            Authentication authentication,
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId
    ) {
        log.info("DELETE /api/customers/{}", customerId);

        customerService.deleteCustomer(customerId);

        return ResponseEntity.noContent().build();
    }
}
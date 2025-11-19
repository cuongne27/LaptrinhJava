package com.evm.backend.service.impl;

import com.evm.backend.dto.request.AssignVehicleRequest;
import com.evm.backend.dto.request.SalesOrderFilterRequest;
import com.evm.backend.dto.request.SalesOrderRequest;
import com.evm.backend.dto.response.SalesOrderDetailResponse;
import com.evm.backend.dto.response.SalesOrderListResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final OrderPromotionsRepository orderPromotionsRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderListResponse> getAllOrders(SalesOrderFilterRequest filterRequest) {
        log.info("Getting orders with filters: {}", filterRequest);

        try {
            // Get total count
            Long total = salesOrderRepository.countOrdersWithFilters(
                    filterRequest.getCustomerId(),
                    filterRequest.getSalesPersonId(),
                    filterRequest.getVehicleId(),
                    filterRequest.getStatus(),
                    filterRequest.getFromDate(),
                    filterRequest.getToDate()
            );
            log.info("Total orders found: {}", total);

            // Get pageable
            Pageable pageable = buildPageable(filterRequest);
            log.info("Pageable: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

            // Get all data (with JOIN FETCH)
            List<SalesOrder> allOrders = salesOrderRepository.findOrdersWithFilters(
                    filterRequest.getCustomerId(),
                    filterRequest.getSalesPersonId(),
                    filterRequest.getVehicleId(),
                    filterRequest.getStatus(),
                    filterRequest.getFromDate(),
                    filterRequest.getToDate()
            );
            log.info("Fetched {} orders from database", allOrders.size());

            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allOrders.size());
            log.info("Pagination: start={}, end={}", start, end);

            List<SalesOrder> pageContent = allOrders.subList(start, end);
            log.info("Page content size: {}", pageContent.size());

            // Convert to response - LOG CHI TIẾT
            List<SalesOrderListResponse> responseList = new ArrayList<>();
            for (SalesOrder order : pageContent) {
                try {
                    log.debug("Converting order ID: {}", order.getId());

                    // Log chi tiết về các entities
                    log.debug("Order vehicle: {}", order.getVehicle());
                    if (order.getVehicle() != null) {
                        log.debug("Vehicle product: {}", order.getVehicle().getProduct());
                        if (order.getVehicle().getProduct() != null) {
                            log.debug("Product brand: {}", order.getVehicle().getProduct().getBrand());
                        }
                    }

                    SalesOrderListResponse response = convertToListResponse(order);
                    responseList.add(response);
                    log.debug("Converted successfully");
                } catch (Exception e) {
                    log.error("Error converting order ID {}: {}", order.getId(), e.getMessage(), e);
                    throw e; // Re-throw để thấy full stack trace
                }
            }

            // Create Page
            Page<SalesOrderListResponse> result = new PageImpl<>(responseList, pageable, total);
            log.info("Returning page with {} elements", result.getContent().size());
            return result;

        } catch (Exception e) {
            log.error("Error in getAllOrders: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<SalesOrderListResponse> getRecentOrders() {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        return salesOrderRepository.findRecentOrders(fromDate)
                .stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderListResponse> getPendingOrders() {
        return salesOrderRepository.findByStatus("PENDING")
                .stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SalesOrderDetailResponse getOrderById(Long orderId) {
        log.info("=== START getOrderById: {} ===", orderId);

        try {
            SalesOrder order = salesOrderRepository.findByIdWithDetails(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

            log.info("Order found: id={}", order.getId());
            log.info("OrderDate: {}", order.getOrderDate());
            log.info("BasePrice: {}", order.getBasePrice());
            log.info("TotalPrice: {}", order.getTotalPrice());
            log.info("Status: {}", order.getStatus());

            log.info("Vehicle: {}", order.getVehicle() != null ? order.getVehicle().getId() : "NULL");
            log.info("Customer: {}", order.getCustomer() != null ? order.getCustomer().getId() : "NULL");
            log.info("SalesPerson: {}", order.getSalesPerson() != null ? order.getSalesPerson().getId() : "NULL");

            if (order.getVehicle() != null) {
                log.info("Vehicle.Product: {}", order.getVehicle().getProduct() != null ? "exists" : "NULL");
                if (order.getVehicle().getProduct() != null) {
                    log.info("Product.Brand: {}", order.getVehicle().getProduct().getBrand() != null ? "exists" : "NULL");
                }
            }

            log.info("Payments size: {}", order.getPayments() != null ? order.getPayments().size() : "NULL");
            log.info("OrderPromotions size: {}", order.getOrderPromotions() != null ? order.getOrderPromotions().size() : "NULL");

            SalesOrderDetailResponse response = convertToDetailResponse(order);
            log.info("=== END getOrderById SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("=== ERROR in getOrderById: {} ===", orderId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse createOrder(SalesOrderRequest request) {
        log.info("Creating sales order for customer: {}", request.getCustomerId());

        String vehicleId = request.getVehicleId();

        // Validate vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Check if vehicle is already sold
        if (salesOrderRepository.existsByVehicleIdAndSold(vehicleId)) {
            throw new IllegalStateException("Vehicle is already sold");
        }

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Validate sales person
        User salesPerson = userRepository.findById(request.getSalesPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Sales person not found"));

        // Create order
        SalesOrder order = SalesOrder.builder()
                .orderDate(request.getOrderDate())
                .basePrice(request.getBasePrice())
                .vat(request.getVat())
                .registrationFee(request.getRegistrationFee() != null ? request.getRegistrationFee() : BigDecimal.ZERO)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .totalPrice(request.getTotalPrice())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .vehicle(vehicle)
                .customer(customer)
                .salesPerson(salesPerson)
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(order);

        // Add promotions if provided
        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            addPromotionsToOrder(savedOrder, request.getPromotionIds());
        }

        log.info("Order created: {}", savedOrder.getId());
        return convertToDetailResponse(savedOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse updateOrder(Long orderId, SalesOrderRequest request) {
        log.info("Updating order: {}", orderId);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate entities
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        User salesPerson = userRepository.findById(request.getSalesPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Sales person not found"));

        // Update order
        order.setOrderDate(request.getOrderDate());
        order.setBasePrice(request.getBasePrice());
        order.setVat(request.getVat());
        order.setRegistrationFee(request.getRegistrationFee() != null ? request.getRegistrationFee() : BigDecimal.ZERO);
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setTotalPrice(request.getTotalPrice());
        order.setStatus(request.getStatus());
        order.setVehicle(vehicle);
        order.setCustomer(customer);
        order.setSalesPerson(salesPerson);

        SalesOrder updated = salesOrderRepository.save(order);
        log.info("Order updated: {}", orderId);
        return convertToDetailResponse(updated);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        salesOrderRepository.save(order);
        log.info("Order cancelled: {}", orderId);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order: {}", orderId);

        if (!salesOrderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        salesOrderRepository.deleteById(orderId);
        log.info("Order deleted: {}", orderId);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status: {} to {}", orderId, status);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        SalesOrder updated = salesOrderRepository.save(order);

        log.info("Order status updated");
        return convertToDetailResponse(updated);
    }

    @Override
    public List<SalesOrderListResponse> getMonthlySales(int year, int month) {
        log.info("Fetching monthly sales: {}/{}", year, month);
        return salesOrderRepository.findMonthlySales(year, month)
                .stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse assignVehicle(Long orderId, AssignVehicleRequest request) {
        log.info("Assigning vehicle {} to order {}", request.getVehicleId(), orderId);

        // 1. Find order
        SalesOrder order = salesOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        // 2. Validate order status
        if (!"PENDING".equals(order.getStatus())) {
            throw new BadRequestException(
                    "Chỉ có thể gán xe cho đơn hàng ở trạng thái PENDING. Trạng thái hiện tại: " + order.getStatus()
            );
        }

        // 3. Check if order already has vehicle
        if (order.getVehicle() != null) {
            throw new BadRequestException(
                    "Đơn hàng đã có xe " + order.getVehicle().getVin() +
                            ". Vui lòng hủy gán xe hiện tại trước khi gán xe mới."
            );
        }

        // 4. Find vehicle
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Xe không tồn tại với VIN: " + request.getVehicleId()));

        // 5. Validate vehicle status
        if (!"AVAILABLE".equals(vehicle.getStatus())) {
            throw new BadRequestException(
                    "Xe không ở trạng thái AVAILABLE. Trạng thái hiện tại: " + vehicle.getStatus()
            );
        }

        // 6. Check if vehicle is already sold
        boolean isVehicleSold = salesOrderRepository.existsByVehicleIdAndSold(vehicle.getId());
        if (isVehicleSold) {
            throw new BadRequestException("Xe đã được bán trong đơn hàng khác");
        }

        // 7. Validate vehicle product matches quotation (if exists)
        // Kiểm tra nếu order có từ quotation thì phải đúng model xe
        if (order.getQuotations() != null && !order.getQuotations().isEmpty()) {
            var quotation = order.getQuotations().iterator().next();
            if (quotation.getProduct() != null && vehicle.getProduct() != null) {
                if (!quotation.getProduct().getId().equals(vehicle.getProduct().getId())) {
                    throw new BadRequestException(
                            "Xe không đúng model. Đơn hàng yêu cầu: " +
                                    quotation.getProduct().getProductName() +
                                    " nhưng xe là: " + vehicle.getProduct().getProductName()
                    );
                }
            }
        }

        // 8. Assign vehicle to order
        order.setVehicle(vehicle);
        order.setStatus("CONFIRMED"); // Chuyển sang CONFIRMED khi đã có xe

        // 9. Update vehicle status
        vehicle.setStatus("RESERVED"); // Đánh dấu xe đã được đặt
        vehicleRepository.save(vehicle);

        // 10. Save order
        SalesOrder savedOrder = salesOrderRepository.save(order);

        log.info("Vehicle {} assigned to order {}. Order status: {} -> CONFIRMED",
                vehicle.getVin(), orderId, "PENDING");

        // 11. Return response
        return convertToDetailResponse(savedOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse unassignVehicle(Long orderId) {
        log.info("Unassigning vehicle from order {}", orderId);

        // 1. Find order
        SalesOrder order = salesOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        // 2. Check if order has vehicle
        if (order.getVehicle() == null) {
            throw new BadRequestException("Đơn hàng chưa có xe để hủy gán");
        }

        // 3. Validate order status
        if ("PAID".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
            throw new BadRequestException(
                    "Không thể hủy gán xe cho đơn hàng đã thanh toán hoặc hoàn thành"
            );
        }

        // 4. Get vehicle
        Vehicle vehicle = order.getVehicle();

        // 5. Unassign vehicle
        order.setVehicle(null);
        order.setStatus("PENDING"); // Chuyển về PENDING

        // 6. Update vehicle status back to AVAILABLE
        vehicle.setStatus("AVAILABLE");
        vehicleRepository.save(vehicle);

        // 7. Save order
        SalesOrder savedOrder = salesOrderRepository.save(order);

        log.info("Vehicle {} unassigned from order {}. Order status: CONFIRMED -> PENDING",
                vehicle.getVin(), orderId);

        return convertToDetailResponse(savedOrder);
    }

    // Helper methods

    private void addPromotionsToOrder(SalesOrder order, java.util.Set<Long> promotionIds) {
        for (Long promotionId : promotionIds) {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

            OrderPromotions orderPromotion = new OrderPromotions();
            orderPromotion.setOrder(order);
            orderPromotion.setPromotion(promotion);
            orderPromotionsRepository.save(orderPromotion);
        }
    }

    private SalesOrderListResponse convertToListResponse(SalesOrder o) {
        log.debug("=== Converting SalesOrder ID: {} ===", o.getId());

        LocalDate now = LocalDate.now();
        int daysFromOrder = (int) ChronoUnit.DAYS.between(o.getOrderDate(), now);

        // Calculate payment info
        BigDecimal paidAmount = calculatePaidAmount(o);
        BigDecimal remainingAmount = o.getTotalPrice().subtract(paidAmount);
        boolean isPaid = remainingAmount.compareTo(BigDecimal.ZERO) <= 0;

        // Null-safe nested access
        Vehicle vehicle = o.getVehicle();
        log.debug("Vehicle: {}", vehicle != null ? vehicle.getId() : "NULL");

        Product product = null;
        Brand brand = null;
        String vehicleModel = null;
        String vehicleBrand = null;
        String vehicleVin = null;

        if (vehicle != null) {
            vehicleVin = vehicle.getVin();
            log.debug("Vehicle VIN: {}", vehicleVin);

            try {
                product = vehicle.getProduct();
                log.debug("Product: {}", product != null ? product.getId() : "NULL");
            } catch (Exception e) {
                log.error("Error loading product: {}", e.getMessage());
            }

            if (product != null) {
                vehicleModel = product.getProductName();
                log.debug("Vehicle Model: {}", vehicleModel);

                try {
                    brand = product.getBrand();
                    log.debug("Brand: {}", brand != null ? brand.getId() : "NULL");
                } catch (Exception e) {
                    log.error("Error loading brand: {}", e.getMessage());
                }

                if (brand != null) {
                    vehicleBrand = brand.getBrandName();
                    log.debug("Brand Name: {}", vehicleBrand);
                }
            }
        }

        SalesOrderListResponse response = SalesOrderListResponse.builder()
                .id(o.getId())
                .orderDate(o.getOrderDate())
                .totalPrice(o.getTotalPrice())
                .status(o.getStatus())
                .vehicleId(vehicle != null ? vehicle.getId() : null)
                .vehicleModel(vehicleModel)
                .vehicleBrand(vehicleBrand)
                .vehicleVin(vehicleVin)
                .customerId(o.getCustomer() != null ? o.getCustomer().getId() : null)
                .customerName(o.getCustomer() != null ? o.getCustomer().getFullName() : null)
                .customerPhone(o.getCustomer() != null ? o.getCustomer().getPhoneNumber() : null)
                .salesPersonId(o.getSalesPerson() != null ? o.getSalesPerson().getId() : null)
                .salesPersonName(o.getSalesPerson() != null ? o.getSalesPerson().getFullName() : null)
                .daysFromOrder(daysFromOrder)
                .isPaid(isPaid)
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .build();

        log.debug("=== Conversion completed ===");
        return response;
    }

    private SalesOrderDetailResponse convertToDetailResponse(SalesOrder o) {
        try {
            log.info("Converting order to detail response...");

            LocalDate now = LocalDate.now();
            int daysFromOrder = o.getOrderDate() != null
                    ? (int) ChronoUnit.DAYS.between(o.getOrderDate(), now)
                    : 0;

            // Calculate payment info
            BigDecimal paidAmount = BigDecimal.ZERO;
            try {
                paidAmount = calculatePaidAmount(o);
                log.info("Calculated paidAmount: {}", paidAmount);
            } catch (Exception e) {
                log.error("Error calculating paid amount", e);
            }

            BigDecimal totalPrice = o.getTotalPrice() != null ? o.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal remainingAmount = totalPrice.subtract(paidAmount);
            boolean isPaid = remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
            boolean canCancel = o.getStatus() != null
                    && !"COMPLETED".equals(o.getStatus())
                    && !"CANCELLED".equals(o.getStatus());

            SalesOrderDetailResponse.SalesOrderDetailResponseBuilder builder = SalesOrderDetailResponse.builder()
                    .id(o.getId())
                    .orderDate(o.getOrderDate())
                    .basePrice(o.getBasePrice() != null ? o.getBasePrice() : BigDecimal.ZERO)
                    .vat(o.getVat() != null ? o.getVat() : BigDecimal.ZERO)
                    .registrationFee(o.getRegistrationFee() != null ? o.getRegistrationFee() : BigDecimal.ZERO)
                    .discountAmount(o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO)
                    .totalPrice(totalPrice)
                    .status(o.getStatus())
                    .daysFromOrder(daysFromOrder)
                    .isPaid(isPaid)
                    .paidAmount(paidAmount)
                    .remainingAmount(remainingAmount)
                    .canCancel(canCancel);

            // Map vehicle info
            try {
                if (o.getVehicle() != null) {
                    log.info("Mapping vehicle info...");
                    Vehicle vehicle = o.getVehicle();

                    builder.vehicleId(vehicle.getId());

                    if (vehicle.getProduct() != null) {
                        Product product = vehicle.getProduct();
                        builder.vehicleModel(product.getProductName());

                        if (product.getBrand() != null) {
                            builder.vehicleBrand(product.getBrand().getBrandName());
                        }
                    }

                    builder.vehicleVin(vehicle.getVin())
                            .vehicleColor(vehicle.getColor());

                    if (vehicle.getManufactureDate() != null) {
                        builder.vehicleYear(vehicle.getManufactureDate().getYear());
                    }

                    log.info("Vehicle info mapped successfully");
                }
            } catch (Exception e) {
                log.error("Error mapping vehicle", e);
            }

            // Map customer info
            try {
                if (o.getCustomer() != null) {
                    log.info("Mapping customer info...");
                    builder.customerId(o.getCustomer().getId())
                            .customerName(o.getCustomer().getFullName())
                            .customerEmail(o.getCustomer().getEmail())
                            .customerPhone(o.getCustomer().getPhoneNumber())
                            .customerAddress(o.getCustomer().getAddress());
                    log.info("Customer info mapped successfully");
                }
            } catch (Exception e) {
                log.error("Error mapping customer", e);
            }

            // Map sales person info
            try {
                if (o.getSalesPerson() != null) {
                    log.info("Mapping salesperson info...");
                    builder.salesPersonId(o.getSalesPerson().getId())
                            .salesPersonName(o.getSalesPerson().getFullName())
                            .salesPersonEmail(o.getSalesPerson().getEmail());
                    log.info("Salesperson info mapped successfully");
                }
            } catch (Exception e) {
                log.error("Error mapping salesperson", e);
            }

            // Map payments
            try {
                if (o.getPayments() != null && !o.getPayments().isEmpty()) {
                    log.info("Mapping {} payments...", o.getPayments().size());
                    Set<SalesOrderDetailResponse.PaymentSummary> payments = o.getPayments().stream()
                            .filter(p -> p != null)
                            .map(p -> SalesOrderDetailResponse.PaymentSummary.builder()
                                    .paymentId(p.getId())
                                    .paymentDate(p.getPaymentDate())
                                    .amount(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                                    .paymentMethod(p.getPaymentMethod())
                                    .status(p.getStatus())
                                    .build())
                            .collect(Collectors.toSet());
                    builder.payments(payments);
                    log.info("Payments mapped successfully");
                }
            } catch (Exception e) {
                log.error("Error mapping payments", e);
                builder.payments(new HashSet<>());
            }

            // Map promotions
            try {
                if (o.getOrderPromotions() != null && !o.getOrderPromotions().isEmpty()) {
                    log.info("Mapping {} promotions...", o.getOrderPromotions().size());
                    Set<SalesOrderDetailResponse.PromotionSummary> promotions = o.getOrderPromotions().stream()
                            .filter(op -> op != null && op.getPromotion() != null)
                            .map(op -> SalesOrderDetailResponse.PromotionSummary.builder()
                                    .promotionId(op.getPromotion().getId())
                                    .promotionName(op.getPromotion().getPromotionName())
                                    .discountType(op.getPromotion().getDiscountType())
                                    .discountValue(op.getPromotion().getDiscountValue() != null ?
                                            op.getPromotion().getDiscountValue() : BigDecimal.ZERO)
                                    .build())
                            .collect(Collectors.toSet());
                    builder.promotions(promotions);
                    log.info("Promotions mapped successfully");
                }
            } catch (Exception e) {
                log.error("Error mapping promotions", e);
                builder.promotions(new HashSet<>());
            }

            log.info("Building final response...");
            return builder.build();

        } catch (Exception e) {
            log.error("FATAL error in convertToDetailResponse", e);
            throw new RuntimeException("Error converting order to response", e);
        }
    }

    private BigDecimal calculatePaidAmount(SalesOrder order) {
        if (order == null) {
            return BigDecimal.ZERO;
        }

        if (order.getPayments() == null || order.getPayments().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return order.getPayments().stream()
                .filter(p -> p != null && "COMPLETED".equals(p.getStatus()))
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Pageable buildPageable(SalesOrderFilterRequest req) {
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 20;
        if (size > 100) size = 100;

        Sort sort = Sort.unsorted();
        if (req.getSortBy() != null) {
            switch (req.getSortBy()) {
                case "date_asc": sort = Sort.by(Sort.Direction.ASC, "orderDate"); break;
                case "date_desc": sort = Sort.by(Sort.Direction.DESC, "orderDate"); break;
                case "price_asc": sort = Sort.by(Sort.Direction.ASC, "totalPrice"); break;
                case "price_desc": sort = Sort.by(Sort.Direction.DESC, "totalPrice"); break;
                case "status_asc": sort = Sort.by(Sort.Direction.ASC, "status"); break;
                case "status_desc": sort = Sort.by(Sort.Direction.DESC, "status"); break;
                default: sort = Sort.by(Sort.Direction.DESC, "orderDate");
            }
        } else {
            sort = Sort.by(Sort.Direction.DESC, "orderDate");
        }

        return PageRequest.of(page, size, sort);
    }
}
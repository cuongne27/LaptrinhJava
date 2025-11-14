package com.evm.backend.service;

import com.evm.backend.dto.request.SalesOrderFilterRequest;
import com.evm.backend.dto.request.SalesOrderRequest;
import com.evm.backend.dto.response.SalesOrderDetailResponse;
import com.evm.backend.dto.response.SalesOrderListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SalesOrderService {

    /**
     * Lấy danh sách orders với filter và pagination
     */
    Page<SalesOrderListResponse> getAllOrders(SalesOrderFilterRequest filterRequest);

    /**
     * Lấy recent orders (7 ngày gần nhất)
     */
    List<SalesOrderListResponse> getRecentOrders();

    /**
     * Lấy pending orders
     */
    List<SalesOrderListResponse> getPendingOrders();

    /**
     * Lấy chi tiết order
     */
    SalesOrderDetailResponse getOrderById(Long orderId);

    /**
     * Tạo order mới
     */
    SalesOrderDetailResponse createOrder(SalesOrderRequest request);

    /**
     * Cập nhật order
     */
    SalesOrderDetailResponse updateOrder(Long orderId, SalesOrderRequest request);

    /**
     * Hủy order
     */
    void cancelOrder(Long orderId);

    /**
     * Xóa order
     */
    void deleteOrder(Long orderId);

    /**
     * Cập nhật status
     */
    SalesOrderDetailResponse updateOrderStatus(Long orderId, String status);

    /**
     * Lấy báo cáo doanh số theo tháng
     */
    List<SalesOrderListResponse> getMonthlySales(int year, int month);
}
package com.evm.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Support Ticket (CREATE & UPDATE)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicketRequest {

    @NotBlank(message = "Tiêu đề ticket không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @Pattern(
            regexp = "OPEN|PENDING|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED",
            message = "Status phải là: OPEN, PENDING, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED"
    )
    private String status; // Default: OPEN

    @NotNull(message = "Customer ID không được để trống")
    private Long customerId;

    private Long assignedUserId; // Staff được assign (nullable khi tạo mới)

    private Long salesOrderId; // Ticket liên quan đến order (nullable)

    private String vehicleId; // Ticket liên quan đến xe (nullable)

    @Pattern(
            regexp = "LOW|MEDIUM|HIGH|URGENT",
            message = "Priority phải là: LOW, MEDIUM, HIGH, URGENT"
    )
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @Pattern(
            regexp = "WARRANTY|TECHNICAL|SALES|DELIVERY|MAINTENANCE|OTHER",
            message = "Category không hợp lệ"
    )
    private String category; // WARRANTY, TECHNICAL, SALES, DELIVERY, MAINTENANCE, OTHER
}
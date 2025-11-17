// 15. SELL_IN_REQUEST_DETAILS Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sell_in_request_details")
public class SellInRequestDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_detail_id")
    private Long id;

    @Column(name = "requested_quantity") // Số lượng yêu cầu
    private Integer requestedQuantity;

    @Column(name = "approved_quantity") // Số lượng được duyệt
    private Integer approvedQuantity;

    @Column(name = "delivered_quantity") // Số lượng đã giao
    private Integer deliveredQuantity;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private SellInRequest sellInRequest;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
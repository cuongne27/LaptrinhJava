// 15. SELL_IN_REQUEST_DETAILS Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sell_in_request_details")
public class SellInRequestDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestDetailID")
    private Long id;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Color", length = 30)
    private String color;

    @ManyToOne
    @JoinColumn(name = "RequestID")
    private SellInRequest sellInRequest;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    private Product product;
}
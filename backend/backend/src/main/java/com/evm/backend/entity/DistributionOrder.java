// 16. DISTRIBUTION_ORDER Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "distribution_order")
public class DistributionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DistributionOrderID")
    private Long id;

    @Column(name = "DealerID")
    private Long dealerId;

    @Column(name = "OrderDate")
    private OffsetDateTime orderDate;

    @Column(name = "ShipmentDate")
    private OffsetDateTime shipmentDate;

    @Column(name = "DeliveryDate")
    private OffsetDateTime deliveryDate;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "TotalQuantity")
    private Integer totalQuantity;

    @Column(name = "TrackingNumber", length = 100)
    private String trackingNumber;

    @ManyToOne
    @JoinColumn(name = "RequestID")
    private SellInRequest sellInRequest;

    @ManyToOne
    @JoinColumn(name = "BrandID")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "ApproverID")
    private User user;
}
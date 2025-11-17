package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sell_in_request")
public class SellInRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "request_number", unique = true, length = 50)
    private String requestNumber; // SIR-2024-00001

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "status", length = 50)
    private String status; // PENDING, APPROVED, REJECTED, IN_TRANSIT, DELIVERED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @ManyToOne
    @JoinColumn(name = "requested_by") // User tạo yêu cầu
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by") // User duyệt yêu cầu
    private User approvedBy;

    @OneToMany(mappedBy = "sellInRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SellInRequestDetails> sellInRequestDetails = new HashSet<>();

    @OneToMany(mappedBy = "sellInRequest")
    @Builder.Default
    private Set<DistributionOrder> distributionOrders = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
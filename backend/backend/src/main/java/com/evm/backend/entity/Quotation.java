package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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
@Table(name = "quotation")
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_id")
    private Long id;

    @Column(name = "quotation_number", unique = true, length = 50)
    private String quotationNumber; // Mã báo giá: QT-2024-00001

    @Column(name = "quotation_date")
    private LocalDate quotationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil; // Hiệu lực đến ngày

    // Giá thành phần
    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice; // Giá xe cơ bản

    @Column(name = "vat", precision = 15, scale = 2)
    private BigDecimal vat; // VAT (10%)

    @Column(name = "registration_fee", precision = 15, scale = 2)
    private BigDecimal registrationFee; // Phí trước bạ

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount; // Giảm giá

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice; // Tổng giá

    @Column(name = "status", length = 50)
    private String status; // DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED, CONVERTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "sales_person_id")
    private User salesPerson;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QuotationPromotion> quotationPromotions = new HashSet<>();

    @Column(name = "sales_order_id")
    private Long salesOrderId;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (quotationDate == null) {
            quotationDate = LocalDate.now();
        }
        if (validUntil == null) {
            validUntil = quotationDate.plusDays(30); // Mặc định hiệu lực 30 ngày
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
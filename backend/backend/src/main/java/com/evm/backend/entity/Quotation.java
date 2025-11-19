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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "quotation")
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "quotation_id")
    private Long id;

    @Column(name = "quotation_number", unique = true, length = 50)
    private String quotationNumber;

    @Column(name = "quotation_date")
    private LocalDate quotationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "vat", precision = 15, scale = 2)
    private BigDecimal vat;

    @Column(name = "registration_fee", precision = 15, scale = 2)
    private BigDecimal registrationFee;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

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

    @OneToMany(
            mappedBy = "quotation",
            cascade = CascadeType.ALL,  // ✅ Đảm bảo có ALL
            orphanRemoval = true,       // ✅ Đảm bảo có orphanRemoval
            fetch = FetchType.LAZY      // ✅ LAZY fetch
    )
    @Builder.Default
    private Set<QuotationPromotion> quotationPromotions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (quotationDate == null) {
            quotationDate = LocalDate.now();
        }
        if (validUntil == null) {
            validUntil = quotationDate.plusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
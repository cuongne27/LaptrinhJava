// 8. SALES_ORDER Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "sales_order")
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_date")
    private LocalDate orderDate;

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
    private String status;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User salesPerson;

    @OneToMany(mappedBy = "order")
    private Set<Payment> payments;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<OrderPromotions> orderPromotions;

    @OneToMany(mappedBy = "salesOrder")
    @Builder.Default
    private Set<Quotation> quotations = new HashSet<>();
}
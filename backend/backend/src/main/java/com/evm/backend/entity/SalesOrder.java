// 8. SALES_ORDER Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sales_order")
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Long id;

    @Column(name = "OrderDate")
    private LocalDate orderDate;

    @Column(name = "BasePrice", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "VAT", precision = 15, scale = 2)
    private BigDecimal vat;

    @Column(name = "RegistrationFee", precision = 15, scale = 2)
    private BigDecimal registrationFee;

    @Column(name = "DiscountAmount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "TotalPrice", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "Status", length = 50)
    private String status;

    @ManyToOne
    @JoinColumn(name = "VehicleID")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "CustomerID")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "SalesPersonID")
    private User salesPerson;

    @OneToMany(mappedBy = "order")
    private Set<Payment> payments;

    @OneToMany(mappedBy = "order")
    private Set<OrderPromotions> orderPromotions;
}
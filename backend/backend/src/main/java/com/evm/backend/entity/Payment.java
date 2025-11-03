// 9. PAYMENT Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Long id;

    @Column(name = "PaymentDate")
    private LocalDate paymentDate;

    @Column(name = "Amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "PaymentMethod", length = 50)
    private String paymentMethod;

    @Column(name = "PaymentType", length = 50)
    private String paymentType;

    @Column(name = "ReferenceNumber", length = 100)
    private String referenceNumber;

    @ManyToOne
    @JoinColumn(name = "OrderID")
    private SalesOrder order;

    @ManyToOne
    @JoinColumn(name = "PayerID")
    private Customer payer;
}
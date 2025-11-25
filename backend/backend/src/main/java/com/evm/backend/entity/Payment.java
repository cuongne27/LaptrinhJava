// 9. PAYMENT Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"order", "payer"}) // Loại trừ quan hệ
@EqualsAndHashCode(exclude = {"order", "payer"})
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_type", length = 50)
    private String paymentType;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "status", length = 30)
    private String status;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private SalesOrder order;

    @ManyToOne
    @JoinColumn(name = "payer_id")
    private Customer payer;
}
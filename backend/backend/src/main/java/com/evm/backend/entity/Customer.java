// 7. CUSTOMER Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Long id;

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "CustomerType", length = 50)
    private String customerType;

    @Column(name = "History", columnDefinition = "TEXT")
    private String history;

    @Column(name = "CreatedAt")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "customer")
    private Set<SalesOrder> salesOrders;

    @OneToMany(mappedBy = "customer")
    private Set<SupportTicket> supportTickets;
}
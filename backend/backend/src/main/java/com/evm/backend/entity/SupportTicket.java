// 12. SUPPORT_TICKET Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "support_ticket")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketID")
    private Long id;

    @Column(name = "Title", length = 200)
    private String title;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CreatedAt")
    private OffsetDateTime createdAt;

    @Column(name = "ClosedAt")
    private OffsetDateTime closedAt;

    @ManyToOne
    @JoinColumn(name = "AssignedUserID")
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "SalesOrderID")
    private SalesOrder salesOrder;

    @ManyToOne
    @JoinColumn(name = "CustomerID")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "VehicleID")
    private Vehicle vehicle;
}
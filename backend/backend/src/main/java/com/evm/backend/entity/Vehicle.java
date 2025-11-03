// 6. VEHICLE Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @Column(name = "VehicleID", length = 50)
    private String id;

    @Column(name = "VIN", length = 50)
    private String vin;

    @Column(name = "BatterySerial", length = 50)
    private String batterySerial;

    @Column(name = "Color", length = 30)
    private String color;

    @Column(name = "ManufactureDate")
    private LocalDate manufactureDate;

    @Column(name = "Status", length = 50)
    private String status;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "DealerID")
    private Dealer dealer;

    @OneToMany(mappedBy = "vehicle")
    private Set<SalesOrder> salesOrders;

    @OneToMany(mappedBy = "vehicle")
    private Set<SupportTicket> supportTickets;
}
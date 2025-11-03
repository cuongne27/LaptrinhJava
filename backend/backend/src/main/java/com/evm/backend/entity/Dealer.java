// 3. DEALER Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dealer")
public class Dealer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DealerID")
    private Long id;

    @Column(name = "DealerName", length = 150)
    private String dealerName;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "DealerLevel", length = 50)
    private String dealerLevel;

    @ManyToOne
    @JoinColumn(name = "BrandID")
    private Brand brand;

    @OneToMany(mappedBy = "dealer")
    private Set<User> users;

    @OneToMany(mappedBy = "dealer")
    private Set<Vehicle> vehicles;

    @OneToMany(mappedBy = "dealer")
    private Set<Appointment> appointments;

    @OneToMany(mappedBy = "dealer")
    private Set<SellInRequest> sellInRequests;

    @OneToMany(mappedBy = "dealer")
    private Set<DealerContract> dealerContracts;
}
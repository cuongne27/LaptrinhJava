// 2. DEALER Entity - FIXED
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dealer")
public class Dealer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dealer_id")
    private Long id;

    @Column(name = "dealer_name", length = 150)
    private String dealerName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dealer_level", length = 50)
    private String dealerLevel;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private Set<Vehicle> vehicles = new HashSet<>();

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private Set<Appointment> appointments = new HashSet<>();

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private Set<SellInRequest> sellInRequests = new HashSet<>();

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private Set<DealerContract> dealerContracts = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dealer)) return false;
        Dealer dealer = (Dealer) o;
        return id != null && id.equals(dealer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
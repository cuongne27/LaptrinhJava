// 4. USER Entity
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
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long id;

    @Column(name = "Username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "PasswordHash", length = 60, nullable = false)
    private String passwordHash;

    @Column(name = "FullName", length = 100, nullable = false)
    private String fullName;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "DateJoined")
    private OffsetDateTime dateJoined;

    @Column(name = "IsActive")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "RoleID", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "BrandID")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "DealerID")
    private Dealer dealer;

    @OneToMany(mappedBy = "user")
    private Set<DistributionOrder> distributionOrders;
}
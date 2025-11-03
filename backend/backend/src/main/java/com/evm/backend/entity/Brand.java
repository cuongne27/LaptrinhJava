// 2. BRAND Entity
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
@Table(name = "brand")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandID")
    private Integer id;

    @Column(name = "BrandName", length = 100)
    private String brandName;

    @Column(name = "HeadquartersAddress", length = 255)
    private String headquartersAddress;

    @Column(name = "TaxCode", length = 20)
    private String taxCode;

    @Column(name = "ContactInfo", length = 255)
    private String contactInfo;

    @OneToMany(mappedBy = "brand")
    private Set<User> users;

    @OneToMany(mappedBy = "brand")
    private Set<Dealer> dealers;

    @OneToMany(mappedBy = "brand")
    private Set<Product> products;

    @OneToMany(mappedBy = "brand")
    private Set<DealerContract> dealerContracts;
}
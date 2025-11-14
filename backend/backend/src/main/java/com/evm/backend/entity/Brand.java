// 1. BRAND Entity - FIXED
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
@Table(name = "brand")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer id;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "headquarters_address", length = 255)
    private String headquartersAddress;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    @OneToMany(mappedBy = "brand")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "brand")
    @Builder.Default
    private Set<Dealer> dealers = new HashSet<>();

    @OneToMany(mappedBy = "brand")
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "brand")
    @Builder.Default
    private Set<DealerContract> dealerContracts = new HashSet<>();

    // CHỈ dùng ID cho hashCode và equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand)) return false;
        Brand brand = (Brand) o;
        return id != null && id.equals(brand.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
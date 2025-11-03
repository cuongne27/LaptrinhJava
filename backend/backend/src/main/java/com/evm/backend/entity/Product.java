// 5. PRODUCT Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Long id;

    @Column(name = "ProductName", length = 150)
    private String productName;

    @Column(name = "Version", length = 50)
    private String version;

    @Column(name = "MSRP", precision = 15, scale = 2)
    private BigDecimal msrp;

    @Column(name = "Specifications", columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ImageURL", length = 255)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "BrandID")
    private Brand brand;

    @OneToMany(mappedBy = "product")
    private Set<Vehicle> vehicles;

    @OneToMany(mappedBy = "product")
    private Set<SellInRequestDetails> sellInRequestDetails;
}
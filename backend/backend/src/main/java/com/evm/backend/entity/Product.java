// 5. PRODUCT Entity
package com.evm.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name", length = 150)
    private String productName;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "msrp", precision = 15, scale = 2)
    private BigDecimal msrp;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "product")
    private Set<Vehicle> vehicles;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Appointment> appointments;

    @OneToMany(mappedBy = "product")
    private Set<SellInRequestDetails> sellInRequestDetails;

    @Embedded
    private TechnicalSpecs technicalSpecs;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference // Để hiển thị 'features' khi get 'Product'
    private List<ProductFeature> features = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference // Để hiển thị 'features' khi get 'Product'
    private List<ProductVariant> variants = new ArrayList<>();
}
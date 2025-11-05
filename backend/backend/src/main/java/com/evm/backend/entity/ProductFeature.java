package com.evm.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_feature")
public class ProductFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Long id;

    @Column(name = "feature_name", length = 100, nullable = false)
    private String featureName; // VD: "Fast Charging", "Smart Technology"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // VD: "Sạc nhanh 80% trong 30 phút"

    @Column(name = "icon_url", length = 255)
    private String iconUrl; // VD: "/icons/fast-charging.svg"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

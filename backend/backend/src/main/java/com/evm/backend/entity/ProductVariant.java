package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for Product Variants (màu sắc)
 * Quan hệ: Product 1-N ProductVariant
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_variant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "color"}))
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @Column(name = "color", length = 50, nullable = false)
    private String color; // VD: "White", "Black", "Red"

    @Column(name = "color_code", length = 7)
    private String colorCode; // VD: "#FFFFFF", "#000000"

    @Column(name = "available_quantity")
    private Long availableQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
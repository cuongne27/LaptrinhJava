// 11. ORDER_PROMOTIONS Entity (Join Table)
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

// Trong OrderPromotions.java
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <--- Thêm vào
@Table(name = "order_promotions")
public class OrderPromotions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_promotion_id")
    @EqualsAndHashCode.Include // <--- Chỉ dùng ID cho equals/hashCode
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // <--- Khuyến nghị dùng LAZY
    @JoinColumn(name = "order_id")
    @ToString.Exclude // <--- Thêm vào
    private SalesOrder order;

    @ManyToOne(fetch = FetchType.LAZY) // <--- Khuyến nghị dùng LAZY
    @JoinColumn(name = "promotion_id")
    @ToString.Exclude // <--- Thêm vào
    private Promotion promotion;
}
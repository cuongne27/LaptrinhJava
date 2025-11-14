// 11. ORDER_PROMOTIONS Entity (Join Table)
package com.evm.backend.entity;

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
@Table(name = "order_promotions")
public class OrderPromotions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_promotion_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private SalesOrder order;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    // OrderPromotions.java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderPromotions)) return false;
        OrderPromotions that = (OrderPromotions) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
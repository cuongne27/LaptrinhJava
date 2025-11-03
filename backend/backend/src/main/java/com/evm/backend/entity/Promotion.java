// 10. PROMOTION Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotion")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionID")
    private Long id;

    @Column(name = "PromotionCode", length = 50)
    private String promotionCode;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "DiscountType", length = 20)
    private String discountType;

    @Column(name = "DiscountValue", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "Conditions", columnDefinition = "TEXT")
    private String conditions;

    @OneToMany(mappedBy = "promotion")
    private Set<OrderPromotions> orderPromotions;
}
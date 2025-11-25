// 10. PROMOTION Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotion")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long id;

    @Column(name = "promotion_code", length = 50)
    private String promotionCode;

    @Column(name = "promotion_name", length = 100)
    private String promotionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", length = 20)
    private String discountType;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @OneToMany(
            mappedBy = "promotion",
            // THÊM: Cấu hình xóa phân tầng
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
//    @Builder.Default
    @ToString.Exclude // <--- Thêm vào
    @EqualsAndHashCode.Exclude // <--- Thêm vào
    private Set<OrderPromotions> orderPromotions;
}
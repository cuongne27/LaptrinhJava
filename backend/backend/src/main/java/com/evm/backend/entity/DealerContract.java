// 17. DEALER_CONTRACT Entity
package com.evm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dealer_contract")
public class DealerContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContractID")
    private Long id;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "ContractTerms", columnDefinition = "TEXT")
    private String contractTerms;

    @Column(name = "CommissionRate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "SalesTarget", precision = 15, scale = 2)
    private BigDecimal salesTarget;

    @ManyToOne
    @JoinColumn(name = "BrandID")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "DealerID")
    private Dealer dealer;
}
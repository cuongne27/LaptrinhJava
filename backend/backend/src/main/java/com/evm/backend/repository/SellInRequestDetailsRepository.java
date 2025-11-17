package com.evm.backend.repository;

import com.evm.backend.entity.Product;
import com.evm.backend.entity.SellInRequest;
import com.evm.backend.entity.SellInRequestDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellInRequestDetailsRepository extends JpaRepository<SellInRequestDetails, Long> {
    List<SellInRequestDetails> findBySellInRequest(SellInRequest sellInRequest);

    List<SellInRequestDetails> findBySellInRequestId(Long requestId);

    List<SellInRequestDetails> findByProduct(Product product);

    List<SellInRequestDetails> findByProductId(Long productId);

    @Query("SELECT sird FROM SellInRequestDetails sird WHERE sird.sellInRequest.id = :requestId AND sird.product.id = :productId")
    List<SellInRequestDetails> findByRequestAndProduct(@Param("requestId") Long requestId,
                                                       @Param("productId") Long productId);

    @Query("SELECT SUM(sird.requestedQuantity) FROM SellInRequestDetails sird WHERE sird.product.id = :productId")
    Integer getTotalRequestedQuantityByProduct(@Param("productId") Long productId);

    @Query("SELECT sird FROM SellInRequestDetails sird WHERE sird.sellInRequest.dealer.id = :dealerId AND sird.product.id = :productId")
    List<SellInRequestDetails> findByDealerAndProduct(@Param("dealerId") Long dealerId,
                                                      @Param("productId") Long productId);
}
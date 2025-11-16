package com.evm.backend.service;

import com.evm.backend.dto.response.ProductComparisonResponse;
import java.util.List;

public interface ProductComparisonService {

    /**
     * So sánh nhiều mẫu xe điện
     * @param productIds danh sách ID sản phẩm cần so sánh (2-3 sản phẩm)
     * @return kết quả so sánh chi tiết
     */
    ProductComparisonResponse compareProducts(List<Long> productIds);

    /**
     * So sánh sản phẩm theo tiêu chí cụ thể
     * @param productIds danh sách ID sản phẩm
     * @param criteria tiêu chí (range, power, battery, price, charging_time)
     * @return kết quả so sánh theo tiêu chí
     */
    ProductComparisonResponse compareProductsByCriteria(List<Long> productIds, String criteria);

    /**
     * So sánh và đề xuất sản phẩm phù hợp theo nhu cầu
     * @param productIds danh sách ID sản phẩm
     * @param userNeeds nhu cầu người dùng (CITY, LONG_DISTANCE, BUDGET, PERFORMANCE)
     * @return kết quả với recommendation
     */
    ProductComparisonResponse compareWithRecommendation(List<Long> productIds, String userNeeds);
}

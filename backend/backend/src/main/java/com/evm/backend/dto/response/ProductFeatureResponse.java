package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Feature
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFeatureResponse {
    private String featureName;
    private String description;
    private String iconUrl;
}

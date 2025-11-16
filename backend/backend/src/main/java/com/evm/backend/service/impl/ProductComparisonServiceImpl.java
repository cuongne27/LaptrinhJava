package com.evm.backend.service.impl;

import com.evm.backend.dto.response.ProductComparisonResponse;
import com.evm.backend.entity.Product;
import com.evm.backend.entity.ProductFeature;
import com.evm.backend.entity.TechnicalSpecs;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.ProductRepository;
import com.evm.backend.service.ProductComparisonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductComparisonServiceImpl implements ProductComparisonService {

    private final ProductRepository productRepository;

    @Override
    public ProductComparisonResponse compareProducts(List<Long> productIds) {
        log.info("So sánh sản phẩm: {}", productIds);

        // Validate
        validateInput(productIds);

        // Lấy thông tin sản phẩm
        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều sản phẩm không tồn tại");
        }

        // Chuyển đổi sang DTO
        List<ProductComparisonResponse.ProductDetail> productDetails =
                products.stream()
                        .map(p -> convertToDetail(p, products))
                        .collect(Collectors.toList());

        // Tạo summary
        ProductComparisonResponse.ComparisonSummary summary = createSummary(products);

        return ProductComparisonResponse.builder()
                .products(productDetails)
                .summary(summary)
                .build();
    }

    @Override
    public ProductComparisonResponse compareProductsByCriteria(List<Long> productIds, String criteria) {
        ProductComparisonResponse response = compareProducts(productIds);

        // Sắp xếp theo tiêu chí
        response.getProducts().sort((p1, p2) -> {
            switch (criteria.toUpperCase()) {
                case "RANGE":
                    return compareIntegerValues(p2.getRange(), p1.getRange());
                case "POWER":
                    return compareIntegerValues(p2.getPower(), p1.getPower());
                case "BATTERY":
                    return compareBigDecimalValues(p2.getBatteryCapacity(), p1.getBatteryCapacity());
                case "PRICE":
                    return compareBigDecimal(p1.getMsrp(), p2.getMsrp()); // Rẻ nhất trước
                case "CHARGING_TIME":
                    return compareIntegerValues(p1.getChargingTime(), p2.getChargingTime()); // Nhanh nhất trước
                default:
                    return 0;
            }
        });

        return response;
    }

    @Override
    public ProductComparisonResponse compareWithRecommendation(List<Long> productIds, String userNeeds) {
        ProductComparisonResponse response = compareProducts(productIds);

        // Thêm recommendation cho từng sản phẩm
        response.getProducts().forEach(product -> {
            String recommendation = generateRecommendation(product, userNeeds);
            product.setRecommendation(recommendation);
        });

        return response;
    }

    // Helper methods

    private void validateInput(List<Long> productIds) {
        if (productIds == null || productIds.size() < 2 || productIds.size() > 3) {
            throw new BadRequestException("Cần chọn từ 2 đến 3 sản phẩm để so sánh");
        }

        // Check duplicates
        if (productIds.size() != new HashSet<>(productIds).size()) {
            throw new BadRequestException("Không được chọn trùng sản phẩm");
        }
    }

    private ProductComparisonResponse.ProductDetail convertToDetail(
            Product product, List<Product> allProducts) {

        TechnicalSpecs specs = product.getTechnicalSpecs();

        ProductComparisonResponse.ProductDetail.ProductDetailBuilder builder =
                ProductComparisonResponse.ProductDetail.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .version(product.getVersion())
                        .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                        .imageUrl(product.getImageUrl())
                        .videoUrl(product.getVideoUrl())
                        .msrp(product.getMsrp());

        // Technical specs - Convert String to appropriate types
        if (specs != null) {
            builder.range(parseInteger(specs.getProductRange()))
                    .power(parseInteger(specs.getPower()))
                    .batteryCapacity(parseBigDecimal(specs.getBatteryCapacity()))
                    .topSpeed(parseInteger(specs.getMaxSpeed()))
                    .acceleration(null) // Không có field này trong TechnicalSpecs
                    .chargingTime(parseInteger(specs.getChargingTime()))
                    .motorType(null) // Không có field này
                    .weight(parseInteger(specs.getWeight()));
        }

        // Features
        List<String> features = new ArrayList<>();
        if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
            features = product.getFeatures().stream()
                    .map(ProductFeature::getFeatureName)
                    .collect(Collectors.toList());
        }
        builder.features(features);

        // Variants
        List<ProductComparisonResponse.VariantInfo> variants = new ArrayList<>();
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variants = product.getVariants().stream()
                    .map(v -> ProductComparisonResponse.VariantInfo.builder()
                            .variantId(v.getId())
                            .variantName(v.getColor())
                            .price(product.getMsrp())
                            .color(v.getColor())
                            .build())
                    .collect(Collectors.toList());
        }
        builder.variants(variants);

        // Calculate advantages and disadvantages
        builder.advantages(calculateAdvantages(product, allProducts))
                .disadvantages(calculateDisadvantages(product, allProducts));

        return builder.build();
    }

    private List<String> calculateAdvantages(Product product, List<Product> allProducts) {
        List<String> advantages = new ArrayList<>();
        TechnicalSpecs specs = product.getTechnicalSpecs();

        if (specs == null) return advantages;

        // Quãng đường
        Integer range = parseInteger(specs.getProductRange());
        if (isMaxInteger(range, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getProductRange() : null))) {
            advantages.add(String.format("🏆 Quãng đường xa nhất: %d km", range));
        }

        // Công suất
        Integer power = parseInteger(specs.getPower());
        if (isMaxInteger(power, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getPower() : null))) {
            advantages.add(String.format("⚡ Công suất mạnh nhất: %d HP", power));
        }

        // Dung lượng pin
        BigDecimal battery = parseBigDecimal(specs.getBatteryCapacity());
        if (isMaxBigDecimal(battery, allProducts,
                p -> parseBigDecimal(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getBatteryCapacity() : null))) {
            advantages.add(String.format("Pin lớn nhất: %.1f kWh", battery));
        }

        // Thời gian sạc
        Integer chargingTime = parseInteger(specs.getChargingTime());
        if (isMinInteger(chargingTime, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getChargingTime() : null))) {
            advantages.add(String.format("Sạc nhanh nhất: %d phút", chargingTime));
        }

        // Giá
        if (product.getMsrp() != null && isMinBigDecimal(product.getMsrp(),
                allProducts, Product::getMsrp)) {
            advantages.add(String.format("Giá rẻ nhất: %s VNĐ",
                    formatPrice(product.getMsrp())));
        }

        // Tốc độ tối đa
        Integer topSpeed = parseInteger(specs.getMaxSpeed());
        if (isMaxInteger(topSpeed, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getMaxSpeed() : null))) {
            advantages.add(String.format("Tốc độ tối đa: %d km/h", topSpeed));
        }

        return advantages;
    }

    private List<String> calculateDisadvantages(Product product, List<Product> allProducts) {
        List<String> disadvantages = new ArrayList<>();
        TechnicalSpecs specs = product.getTechnicalSpecs();

        if (specs == null) return disadvantages;

        // Quãng đường ngắn nhất
        Integer range = parseInteger(specs.getProductRange());
        if (isMinInteger(range, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getProductRange() : null))) {
            disadvantages.add(String.format("Quãng đường ngắn nhất: %d km", range));
        }

        // Công suất thấp nhất
        Integer power = parseInteger(specs.getPower());
        if (isMinInteger(power, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getPower() : null))) {
            disadvantages.add(String.format("Công suất thấp nhất: %d HP", power));
        }

        // Pin nhỏ nhất
        BigDecimal battery = parseBigDecimal(specs.getBatteryCapacity());
        if (isMinBigDecimal(battery, allProducts,
                p -> parseBigDecimal(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getBatteryCapacity() : null))) {
            disadvantages.add(String.format("Pin nhỏ nhất: %.1f kWh", battery));
        }

        // Sạc lâu nhất
        Integer chargingTime = parseInteger(specs.getChargingTime());
        if (isMaxInteger(chargingTime, allProducts,
                p -> parseInteger(p.getTechnicalSpecs() != null ?
                        p.getTechnicalSpecs().getChargingTime() : null))) {
            disadvantages.add(String.format("Sạc lâu nhất: %d phút", chargingTime));
        }

        // Giá đắt nhất
        if (product.getMsrp() != null && isMaxBigDecimal(product.getMsrp(),
                allProducts, Product::getMsrp)) {
            disadvantages.add(String.format("Giá đắt nhất: %s VNĐ",
                    formatPrice(product.getMsrp())));
        }

        return disadvantages;
    }

    private ProductComparisonResponse.ComparisonSummary createSummary(List<Product> products) {
        return ProductComparisonResponse.ComparisonSummary.builder()
                .bestRange(findBest(products, "RANGE"))
                .bestPower(findBest(products, "POWER"))
                .bestBattery(findBest(products, "BATTERY"))
                .fastestCharging(findBest(products, "CHARGING"))
                .cheapest(findBest(products, "CHEAPEST"))
                .mostExpensive(findBest(products, "EXPENSIVE"))
                .bestValue(findBest(products, "VALUE"))
                .build();
    }

    private ProductComparisonResponse.ComparisonSummary.ProductBest findBest(
            List<Product> products, String criteria) {

        Product best = null;
        Object value = null;

        switch (criteria) {
            case "RANGE":
                best = products.stream()
                        .filter(p -> p.getTechnicalSpecs() != null &&
                                parseInteger(p.getTechnicalSpecs().getProductRange()) != null)
                        .max(Comparator.comparing(p ->
                                parseInteger(p.getTechnicalSpecs().getProductRange())))
                        .orElse(null);
                if (best != null)
                    value = parseInteger(best.getTechnicalSpecs().getProductRange()) + " km";
                break;

            case "POWER":
                best = products.stream()
                        .filter(p -> p.getTechnicalSpecs() != null &&
                                parseInteger(p.getTechnicalSpecs().getPower()) != null)
                        .max(Comparator.comparing(p ->
                                parseInteger(p.getTechnicalSpecs().getPower())))
                        .orElse(null);
                if (best != null)
                    value = parseInteger(best.getTechnicalSpecs().getPower()) + " HP";
                break;

            case "BATTERY":
                best = products.stream()
                        .filter(p -> p.getTechnicalSpecs() != null &&
                                parseBigDecimal(p.getTechnicalSpecs().getBatteryCapacity()) != null)
                        .max(Comparator.comparing(p ->
                                parseBigDecimal(p.getTechnicalSpecs().getBatteryCapacity())))
                        .orElse(null);
                if (best != null)
                    value = parseBigDecimal(best.getTechnicalSpecs().getBatteryCapacity()) + " kWh";
                break;

            case "CHARGING":
                best = products.stream()
                        .filter(p -> p.getTechnicalSpecs() != null &&
                                parseInteger(p.getTechnicalSpecs().getChargingTime()) != null)
                        .min(Comparator.comparing(p ->
                                parseInteger(p.getTechnicalSpecs().getChargingTime())))
                        .orElse(null);
                if (best != null)
                    value = parseInteger(best.getTechnicalSpecs().getChargingTime()) + " phút";
                break;

            case "CHEAPEST":
                best = products.stream()
                        .filter(p -> p.getMsrp() != null)
                        .min(Comparator.comparing(Product::getMsrp))
                        .orElse(null);
                if (best != null) value = formatPrice(best.getMsrp()) + " VNĐ";
                break;

            case "EXPENSIVE":
                best = products.stream()
                        .filter(p -> p.getMsrp() != null)
                        .max(Comparator.comparing(Product::getMsrp))
                        .orElse(null);
                if (best != null) value = formatPrice(best.getMsrp()) + " VNĐ";
                break;

            case "VALUE":
                best = products.stream()
                        .filter(p -> p.getMsrp() != null &&
                                p.getTechnicalSpecs() != null)
                        .max(Comparator.comparing(this::calculateValueScore))
                        .orElse(null);
                if (best != null) value = "Tốt nhất về tổng thể";
                break;
        }

        if (best == null) return null;

        return ProductComparisonResponse.ComparisonSummary.ProductBest.builder()
                .productId(best.getId())
                .productName(best.getProductName())
                .value(value)
                .build();
    }

    private double calculateValueScore(Product product) {
        TechnicalSpecs specs = product.getTechnicalSpecs();
        if (specs == null || product.getMsrp() == null) return 0;

        Integer rangeInt = parseInteger(specs.getProductRange());
        Integer powerInt = parseInteger(specs.getPower());

        double range = rangeInt != null ? rangeInt : 0;
        double power = powerInt != null ? powerInt / 10.0 : 0;
        double price = product.getMsrp().doubleValue();

        if (price == 0) return 0;
        return (range + power) / (price / 1000000);
    }

    private String generateRecommendation(ProductComparisonResponse.ProductDetail product, String userNeeds) {
        return switch (userNeeds.toUpperCase()) {
            case "CITY" -> "Phù hợp đi trong thành phố, quãng đường ngắn hàng ngày";
            case "LONG_DISTANCE" -> {
                if (product.getRange() != null && product.getRange() > 400) {
                    yield "Tuyệt vời cho đường dài, quãng đường lên tới " + product.getRange() + " km";
                }
                yield "Không phù hợp cho đường dài";
            }
            case "BUDGET" -> "Lựa chọn tiết kiệm, phù hợp cho ngân sách hạn chế";
            case "PERFORMANCE" -> {
                if (product.getPower() != null && product.getPower() > 150) {
                    yield "Hiệu suất cao, phù hợp cho người thích tốc độ";
                }
                yield "Hiệu suất trung bình";
            }
            default -> "Sản phẩm chất lượng";
        };
    }

    // Utility methods - String parsing

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            // Remove any non-digit characters except minus sign
            String cleaned = value.replaceAll("[^0-9-]", "");
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse integer from: {}", value);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            // Remove any non-numeric characters except decimal point and minus
            String cleaned = value.replaceAll("[^0-9.-]", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse decimal from: {}", value);
            return null;
        }
    }

    // Comparison methods for Integer

    private boolean isMaxInteger(Integer value, List<Product> products,
                                 java.util.function.Function<Product, Integer> extractor) {
        if (value == null) return false;
        return products.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(max -> max.equals(value))
                .orElse(false);
    }

    private boolean isMinInteger(Integer value, List<Product> products,
                                 java.util.function.Function<Product, Integer> extractor) {
        if (value == null) return false;
        return products.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .map(min -> min.equals(value))
                .orElse(false);
    }

    // Comparison methods for BigDecimal

    private boolean isMaxBigDecimal(BigDecimal value, List<Product> products,
                                    java.util.function.Function<Product, BigDecimal> extractor) {
        if (value == null) return false;
        return products.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(max -> max.compareTo(value) == 0)
                .orElse(false);
    }

    private boolean isMinBigDecimal(BigDecimal value, List<Product> products,
                                    java.util.function.Function<Product, BigDecimal> extractor) {
        if (value == null) return false;
        return products.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .map(min -> min.compareTo(value) == 0)
                .orElse(false);
    }

    private int compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    private int compareIntegerValues(Integer a, Integer b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    private int compareBigDecimalValues(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "N/A";
        return String.format("%,.0f", price);
    }
}

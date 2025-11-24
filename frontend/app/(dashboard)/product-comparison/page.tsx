"use client";

import { useState, useEffect } from "react";
import { apiClient } from "@/lib/api/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import toast from "react-hot-toast";
import { formatCurrency } from "@/lib/utils";
import type { Product, PaginatedResponse } from "@/types";
import { Search, RefreshCw, GitCompare, TrendingUp, Award, X } from "lucide-react";

interface ProductComparisonResponse {
  products: Array<{
    id: number;
    productName: string;
    brandName: string;
    msrp: number;
    range: number;
    power: number;
    batteryCapacity: number;
    chargingTime: number;
    imageUrl?: string;
    variants: Array<{
      color: string;
      colorCode: string;
      availableQuantity: number;
    }>;
    highlights: {
      isBestRange?: boolean;
      isBestPower?: boolean;
      isBestBattery?: boolean;
      isBestPrice?: boolean;
      isBestCharging?: boolean;
    };
    recommendation?: string;
  }>;
  summary: {
    bestRange?: { productId: number; productName: string; value: number | string };
    bestPower?: { productId: number; productName: string; value: number | string };
    bestBattery?: { productId: number; productName: string; value: number | string };
    cheapest?: { productId: number; productName: string; value: number | string };
    fastestCharging?: { productId: number; productName: string; value: number | string };
  };
}

export default function ProductComparisonPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([]);
  const [comparisonResult, setComparisonResult] = useState<ProductComparisonResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [comparing, setComparing] = useState(false);
  const [search, setSearch] = useState("");
  const [criteria, setCriteria] = useState<string>("");
  const [userNeeds, setUserNeeds] = useState<string>("");
  const [page, setPage] = useState(0);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  useEffect(() => {
    fetchProducts();
  }, [page, search, refreshTrigger]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "50",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<Product>>(
        `/products?${params.toString()}`
      );
      setProducts(response.data.content);
    } catch (error) {
      console.error("Error fetching products:", error);
      toast.error("Không thể tải danh sách sản phẩm");
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    setSearch("");
    setPage(0);
    setRefreshTrigger(prev => prev + 1);
  };

  const toggleProductSelection = (productId: number) => {
    if (selectedProductIds.includes(productId)) {
      setSelectedProductIds(selectedProductIds.filter((id) => id !== productId));
    } else {
      if (selectedProductIds.length >= 3) {
        toast.error("Chỉ có thể so sánh tối đa 3 sản phẩm");
        return;
      }
      setSelectedProductIds([...selectedProductIds, productId]);
    }
  };

  const handleCompare = async () => {
    if (selectedProductIds.length < 2) {
      toast.error("Vui lòng chọn ít nhất 2 sản phẩm để so sánh");
      return;
    }
    if (selectedProductIds.length > 3) {
      toast.error("Chỉ có thể so sánh tối đa 3 sản phẩm");
      return;
    }

    try {
      setComparing(true);
      const productIdsParam = selectedProductIds.join(",");
      let url = `/product-comparisons?productIds=${productIdsParam}`;

      if (criteria) {
        url = `/product-comparisons/by-criteria?productIds=${productIdsParam}&criteria=${criteria}`;
      } else if (userNeeds) {
        url = `/product-comparisons/with-recommendation?productIds=${productIdsParam}&userNeeds=${userNeeds}`;
      }

      const response = await apiClient.get<ProductComparisonResponse>(url);
      console.log("Comparison response:", response.data);
      setComparisonResult(response.data);
      toast.success("So sánh thành công!");
    } catch (error: any) {
      console.error("Error comparing products:", error);
      toast.error(error.response?.data?.message || "Không thể so sánh sản phẩm");
    } finally {
      setComparing(false);
    }
  };

  const clearSelection = () => {
    setSelectedProductIds([]);
    setComparisonResult(null);
    setCriteria("");
    setUserNeeds("");
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">So sánh sản phẩm</h1>
          <p className="text-muted-foreground">So sánh các mẫu xe điện về thông số kỹ thuật và giá</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleRefresh} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Làm mới
          </Button>
          {selectedProductIds.length > 0 && (
            <Button variant="outline" onClick={clearSelection}>
              <X className="mr-2 h-4 w-4" />
              Xóa lựa chọn
            </Button>
          )}
        </div>
      </div>

      {/* Search Products */}
      <Card>
        <CardHeader>
          <CardTitle>Tìm kiếm sản phẩm</CardTitle>
          <CardDescription>Chọn 2-3 sản phẩm để so sánh</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm sản phẩm..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="pl-9"
            />
          </div>

          <div className="grid gap-2 max-h-64 overflow-y-auto">
            {products.map((product) => {
              const isSelected = selectedProductIds.includes(product.id);
              return (
                <div
                  key={product.id}
                  onClick={() => toggleProductSelection(product.id)}
                  className={`flex items-center justify-between p-3 border rounded-lg cursor-pointer transition-colors ${
                    isSelected
                      ? "bg-primary text-primary-foreground border-primary"
                      : "hover:bg-accent"
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`h-5 w-5 rounded border-2 flex items-center justify-center ${
                        isSelected
                          ? "border-primary-foreground bg-primary-foreground"
                          : "border-muted-foreground"
                      }`}
                    >
                      {isSelected && <div className="h-2 w-2 rounded-full bg-primary" />}
                    </div>
                    <div>
                      <p className="font-medium">{product.productName}</p>
                      <p className={`text-sm ${isSelected ? "text-primary-foreground/80" : "text-muted-foreground"}`}>
                        {product.brandName} • {formatCurrency(product.msrp)}
                      </p>
                    </div>
                  </div>
                  {isSelected && (
                    <span className="text-xs bg-primary-foreground/20 px-2 py-1 rounded">
                      Đã chọn
                    </span>
                  )}
                </div>
              );
            })}
          </div>

          {selectedProductIds.length > 0 && (
            <div className="pt-4 border-t">
              <p className="text-sm font-medium mb-2">
                Đã chọn {selectedProductIds.length}/3 sản phẩm:
              </p>
              <div className="flex flex-wrap gap-2">
                {selectedProductIds.map((id) => {
                  const product = products.find((p) => p.id === id);
                  return (
                    <div
                      key={id}
                      className="flex items-center gap-2 bg-primary/10 text-primary px-3 py-1 rounded-full text-sm"
                    >
                      {product?.productName}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleProductSelection(id);
                        }}
                        className="hover:bg-primary/20 rounded-full p-0.5"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Comparison Options */}
      {selectedProductIds.length >= 2 && (
        <Card>
          <CardHeader>
            <CardTitle>Tùy chọn so sánh</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">So sánh theo tiêu chí</label>
                <select
                  value={criteria}
                  onChange={(e) => {
                    setCriteria(e.target.value);
                    setUserNeeds("");
                  }}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="">So sánh tổng quát</option>
                  <option value="RANGE">Tầm hoạt động (Range)</option>
                  <option value="POWER">Công suất (Power)</option>
                  <option value="BATTERY">Dung lượng pin (Battery)</option>
                  <option value="PRICE">Giá (Price)</option>
                  <option value="CHARGING_TIME">Thời gian sạc (Charging Time)</option>
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">So sánh với khuyến nghị</label>
                <select
                  value={userNeeds}
                  onChange={(e) => {
                    setUserNeeds(e.target.value);
                    setCriteria("");
                  }}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="">Không có</option>
                  <option value="CITY">Đi lại trong thành phố</option>
                  <option value="LONG_DISTANCE">Đi đường dài</option>
                  <option value="BUDGET">Tiết kiệm ngân sách</option>
                  <option value="PERFORMANCE">Hiệu suất cao</option>
                </select>
              </div>
            </div>
            <Button onClick={handleCompare} disabled={comparing} className="w-full" size="lg">
              <GitCompare className="mr-2 h-5 w-5" />
              {comparing ? "Đang so sánh..." : "So sánh sản phẩm"}
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Comparison Results */}
      {comparisonResult && (
        <div className="space-y-6">
          {/* Summary */}
          <Card>
            <CardHeader>
              <CardTitle>Tóm tắt so sánh</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                {comparisonResult.summary.bestRange && (
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <Award className="h-6 w-6 mx-auto mb-2 text-yellow-500" />
                    <p className="text-xs text-muted-foreground mb-1">Tầm hoạt động tốt nhất</p>
                    <p className="font-semibold">{comparisonResult.summary.bestRange.productName}</p>
                    <p className="text-sm text-muted-foreground">
                      {comparisonResult.summary.bestRange.value} km
                    </p>
                  </div>
                )}
                {comparisonResult.summary.bestPower && (
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <Award className="h-6 w-6 mx-auto mb-2 text-blue-500" />
                    <p className="text-xs text-muted-foreground mb-1">Công suất tốt nhất</p>
                    <p className="font-semibold">{comparisonResult.summary.bestPower.productName}</p>
                    <p className="text-sm text-muted-foreground">
                      {comparisonResult.summary.bestPower.value} kW
                    </p>
                  </div>
                )}
                {comparisonResult.summary.bestBattery && (
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <Award className="h-6 w-6 mx-auto mb-2 text-green-500" />
                    <p className="text-xs text-muted-foreground mb-1">Pin tốt nhất</p>
                    <p className="font-semibold">{comparisonResult.summary.bestBattery.productName}</p>
                    <p className="text-sm text-muted-foreground">
                      {comparisonResult.summary.bestBattery.value} kWh
                    </p>
                  </div>
                )}
                {comparisonResult.summary.cheapest && (
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <Award className="h-6 w-6 mx-auto mb-2 text-purple-500" />
                    <p className="text-xs text-muted-foreground mb-1">Giá tốt nhất</p>
                    <p className="font-semibold">{comparisonResult.summary.cheapest.productName}</p>
                    <p className="text-sm text-muted-foreground">
                      {typeof comparisonResult.summary.cheapest.value === "number"
                        ? formatCurrency(comparisonResult.summary.cheapest.value)
                        : comparisonResult.summary.cheapest.value}
                    </p>
                  </div>
                )}
                {comparisonResult.summary.fastestCharging && (
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <Award className="h-6 w-6 mx-auto mb-2 text-orange-500" />
                    <p className="text-xs text-muted-foreground mb-1">Sạc nhanh nhất</p>
                    <p className="font-semibold">{comparisonResult.summary.fastestCharging.productName}</p>
                    <p className="text-sm text-muted-foreground">
                      {comparisonResult.summary.fastestCharging.value} phút
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Detailed Comparison */}
          <Card>
            <CardHeader>
              <CardTitle>Chi tiết so sánh</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left p-3">Thông số</th>
                      {comparisonResult.products.map((product) => (
                        <th key={product.id} className="text-center p-3">
                          <div>
                            <p className="font-semibold">{product.productName}</p>
                            <p className="text-xs text-muted-foreground">{product.brandName}</p>
                          </div>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    <tr className="border-b">
                      <td className="p-3 font-medium">Giá</td>
                      {comparisonResult.products.map((product) => (
                        <td key={product.id} className="p-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            {formatCurrency(product.msrp)}
                            {product.highlights?.isBestPrice && (
                              <Award className="h-4 w-4 text-purple-500" />
                            )}
                          </div>
                        </td>
                      ))}
                    </tr>
                    <tr className="border-b">
                      <td className="p-3 font-medium">Tầm hoạt động (km)</td>
                      {comparisonResult.products.map((product) => (
                        <td key={product.id} className="p-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            {product.range}
                            {product.highlights?.isBestRange && (
                              <Award className="h-4 w-4 text-yellow-500" />
                            )}
                          </div>
                        </td>
                      ))}
                    </tr>
                    <tr className="border-b">
                      <td className="p-3 font-medium">Công suất (kW)</td>
                      {comparisonResult.products.map((product) => (
                        <td key={product.id} className="p-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            {product.power}
                            {product.highlights?.isBestPower && (
                              <Award className="h-4 w-4 text-blue-500" />
                            )}
                          </div>
                        </td>
                      ))}
                    </tr>
                    <tr className="border-b">
                      <td className="p-3 font-medium">Dung lượng pin (kWh)</td>
                      {comparisonResult.products.map((product) => (
                        <td key={product.id} className="p-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            {product.batteryCapacity}
                            {product.highlights?.isBestBattery && (
                              <Award className="h-4 w-4 text-green-500" />
                            )}
                          </div>
                        </td>
                      ))}
                    </tr>
                    <tr className="border-b">
                      <td className="p-3 font-medium">Thời gian sạc (phút)</td>
                      {comparisonResult.products.map((product) => (
                        <td key={product.id} className="p-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            {product.chargingTime}
                            {product.highlights?.isBestCharging && (
                              <Award className="h-4 w-4 text-orange-500" />
                            )}
                          </div>
                        </td>
                      ))}
                    </tr>
                    {comparisonResult.products.some((p) => p.recommendation) && (
                      <tr className="border-b bg-muted/50">
                        <td className="p-3 font-medium">Khuyến nghị</td>
                        {comparisonResult.products.map((product) => (
                          <td key={product.id} className="p-3 text-center">
                            {product.recommendation && (
                              <div className="flex items-center justify-center gap-2">
                                <TrendingUp className="h-4 w-4 text-primary" />
                                <span className="text-sm">{product.recommendation}</span>
                              </div>
                            )}
                          </td>
                        ))}
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
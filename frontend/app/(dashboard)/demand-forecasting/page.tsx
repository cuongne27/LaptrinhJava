"use client";

import { useState, useEffect } from "react";
import { apiClient } from "@/lib/api/client";
import { EntityModal } from "@/components/entity/EntityModal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import toast from "react-hot-toast";
import { formatDate } from "@/lib/utils";
import type { PaginatedResponse } from "@/types";
import { 
  Search, Plus, Eye, Edit, Trash2, RefreshCw, Brain, 
  TrendingUp, TrendingDown, Minus, Target, AlertCircle,
  Calendar, Package
} from "lucide-react";

interface DemandForecast {
  id: number;
  productId: number;
  productName: string;
  productVersion?: string;
  brandName: string;
  forecastPeriod: string;
  forecastDate: string;
  predictedDemand: number;
  confidenceScore: number;
  actualDemand?: number;
  accuracy?: number;
  forecastMethod: string;
  historicalDataPoints?: number;
  seasonalityFactor?: number;
  trendFactor?: number;
  marketGrowthRate?: number;
  status: string;
  notes?: string;
  createdByName?: string;
  createdAt: string;
  updatedAt: string;
  insights?: {
    trend: string;
    trendPercentage: number;
    seasonalPattern: string;
    influencingFactors: string[];
    recommendation: string;
  };
}

const forecastSchema = z.object({
  productId: z.number().min(1, "Vui lòng chọn sản phẩm"),
  forecastPeriod: z.string().min(1, "Vui lòng chọn chu kỳ dự báo"),
  forecastDate: z.string().min(1, "Vui lòng chọn ngày dự báo"),
  numberOfPeriods: z.number().min(1).max(24).optional(),
  forecastMethod: z.string().optional(),
  notes: z.string().optional(),
});

type ForecastForm = z.infer<typeof forecastSchema>;

export default function DemandForecastingPage() {
  const [forecasts, setForecasts] = useState<DemandForecast[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedForecast, setSelectedForecast] = useState<DemandForecast | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ForecastForm>({
    resolver: zodResolver(forecastSchema),
    defaultValues: {
      forecastPeriod: "MONTHLY",
      forecastDate: new Date().toISOString().split("T")[0],
      numberOfPeriods: 3,
      forecastMethod: "LINEAR_REGRESSION",
    }
  });

  useEffect(() => {
    fetchForecasts();
  }, [page, search]);

  const fetchForecasts = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
        sortBy: "forecast_date_desc"
      });
      
      if (search) {
        params.append("searchKeyword", search);
      }

      const response = await apiClient.get<PaginatedResponse<DemandForecast>>(
        `/demand-forecasts?${params.toString()}`
      );
      setForecasts(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      console.error("Error fetching forecasts:", error);
      toast.error("Không thể tải danh sách dự báo");
      setForecasts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    reset({
      productId: 0,
      forecastPeriod: "MONTHLY",
      forecastDate: new Date().toISOString().split("T")[0],
      numberOfPeriods: 3,
      forecastMethod: "LINEAR_REGRESSION",
      notes: "",
    });
    setSelectedForecast(null);
    setViewMode("create");
  };

  const handleView = (forecast: DemandForecast) => {
    setSelectedForecast(forecast);
    setViewMode("detail");
  };

  const handleDelete = async (forecast: DemandForecast) => {
    if (!confirm(`Bạn có chắc muốn xóa dự báo cho "${forecast.productName}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/demand-forecasts/${forecast.id}`);
      toast.success("Xóa thành công!");
      fetchForecasts();
    } catch (error: any) {
      console.error("Error deleting forecast:", error);
      const errorMessage = error.response?.data?.message || "Không thể xóa dự báo";
      toast.error(errorMessage);
    }
  };

  const handleUpdateActualDemand = async (forecastId: number, actualDemand: number) => {
    try {
      await apiClient.patch(`/demand-forecasts/${forecastId}/actual-demand`, null, {
        params: { actualDemand }
      });
      toast.success("Cập nhật nhu cầu thực tế thành công!");
      fetchForecasts();
    } catch (error: any) {
      console.error("Error updating actual demand:", error);
      toast.error("Không thể cập nhật nhu cầu thực tế");
    }
  };

  const onSubmit = async (data: ForecastForm) => {
    try {
      const payload = {
        productId: data.productId,
        forecastPeriod: data.forecastPeriod,
        forecastDate: data.forecastDate,
        numberOfPeriods: data.numberOfPeriods || 3,
        forecastMethod: data.forecastMethod || "LINEAR_REGRESSION",
        notes: data.notes,
      };

      // Get current user ID (replace with actual auth)
      const userId = 1;

      await apiClient.post(`/demand-forecasts?userId=${userId}`, payload);
      toast.success("Tạo dự báo thành công!");
      
      setViewMode("list");
      reset();
      fetchForecasts();
    } catch (error: any) {
      console.error("Error creating forecast:", error);
      const errorMessage = error.response?.data?.message || "Không thể tạo dự báo";
      toast.error(errorMessage);
    }
  };

  const getTrendIcon = (trend?: string) => {
    if (trend === "INCREASING") return <TrendingUp className="h-5 w-5 text-green-500" />;
    if (trend === "DECREASING") return <TrendingDown className="h-5 w-5 text-red-500" />;
    return <Minus className="h-5 w-5 text-gray-500" />;
  };

  const getConfidenceColor = (score: number) => {
    if (score >= 85) return "bg-green-50 text-green-700 border-green-200";
    if (score >= 70) return "bg-yellow-50 text-yellow-700 border-yellow-200";
    return "bg-red-50 text-red-700 border-red-200";
  };

  const getAccuracyColor = (accuracy?: number) => {
    if (!accuracy) return "text-gray-500";
    if (accuracy >= 90) return "text-green-600";
    if (accuracy >= 80) return "text-yellow-600";
    return "text-red-600";
  };

  const getPeriodLabel = (period: string) => {
    const labels: Record<string, string> = {
      MONTHLY: "Hàng tháng",
      QUARTERLY: "Hàng quý",
      YEARLY: "Hàng năm",
    };
    return labels[period] || period;
  };

  const getMethodLabel = (method: string) => {
    const labels: Record<string, string> = {
      LINEAR_REGRESSION: "Hồi quy tuyến tính",
      MOVING_AVERAGE: "Trung bình trượt",
      EXPONENTIAL_SMOOTHING: "Làm mượt hàm mũ",
    };
    return labels[method] || method;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">AI Dự báo Nhu cầu</h1>
          <p className="text-muted-foreground">Dự báo nhu cầu thị trường bằng Machine Learning</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchForecasts} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Làm mới
          </Button>
          <Button onClick={handleCreate}>
            <Brain className="mr-2 h-4 w-4" />
            Tạo Dự báo
          </Button>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Tổng Dự báo</p>
                <p className="text-2xl font-bold">{forecasts.length}</p>
              </div>
              <Target className="h-8 w-8 text-blue-500 opacity-20" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Độ chính xác TB</p>
                <p className="text-2xl font-bold text-green-600">
                  {forecasts.filter(f => f.accuracy).length > 0
                    ? Math.round(
                        forecasts
                          .filter(f => f.accuracy)
                          .reduce((sum, f) => sum + (f.accuracy || 0), 0) /
                          forecasts.filter(f => f.accuracy).length
                      )
                    : 0}
                  %
                </p>
              </div>
              <TrendingUp className="h-8 w-8 text-green-500 opacity-20" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Xu hướng Tăng</p>
                <p className="text-2xl font-bold text-purple-600">
                  {forecasts.filter(f => f.insights?.trend === "INCREASING").length}
                </p>
              </div>
              <TrendingUp className="h-8 w-8 text-purple-500 opacity-20" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Cần Xem xét</p>
                <p className="text-2xl font-bold text-orange-600">
                  {forecasts.filter(f => (f.confidenceScore || 0) < 70).length}
                </p>
              </div>
              <AlertCircle className="h-8 w-8 text-orange-500 opacity-20" />
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Tìm kiếm</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm theo tên sản phẩm..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="pl-9"
            />
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Đang tải...</div>
        </div>
      ) : (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Danh sách dự báo ({forecasts.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {forecasts.map((forecast) => (
                  <div
                    key={forecast.id}
                    className="flex items-start justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex-1">
                      <div className="flex items-start gap-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-purple-500 text-white">
                          <Brain className="h-6 w-6" />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-3">
                            <h3 className="font-semibold">{forecast.productName}</h3>
                            {forecast.productVersion && (
                              <span className="text-sm text-muted-foreground">
                                {forecast.productVersion}
                              </span>
                            )}
                            {getTrendIcon(forecast.insights?.trend)}
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                            <span>{forecast.brandName}</span>
                            <span>•</span>
                            <span>{getPeriodLabel(forecast.forecastPeriod)}</span>
                            <span>•</span>
                            <span>{formatDate(forecast.forecastDate)}</span>
                          </div>
                          
                          {/* Prediction vs Actual */}
                          <div className="mt-3 flex items-center gap-6">
                            <div className="flex items-center gap-2">
                              <Target className="h-4 w-4 text-blue-500" />
                              <div>
                                <p className="text-xs text-muted-foreground">Dự báo</p>
                                <p className="text-lg font-bold text-blue-600">
                                  {forecast.predictedDemand}
                                </p>
                              </div>
                            </div>
                            
                            {forecast.actualDemand && (
                              <>
                                <span className="text-muted-foreground">vs</span>
                                <div className="flex items-center gap-2">
                                  <Package className="h-4 w-4 text-green-500" />
                                  <div>
                                    <p className="text-xs text-muted-foreground">Thực tế</p>
                                    <p className="text-lg font-bold text-green-600">
                                      {forecast.actualDemand}
                                    </p>
                                  </div>
                                </div>
                                {forecast.accuracy && (
                                  <div>
                                    <p className="text-xs text-muted-foreground">Độ chính xác</p>
                                    <p className={`text-sm font-semibold ${getAccuracyColor(forecast.accuracy)}`}>
                                      {forecast.accuracy}%
                                    </p>
                                  </div>
                                )}
                              </>
                            )}
                          </div>

                          {/* Confidence & Method */}
                          <div className="mt-3 flex items-center gap-4">
                            <span className={`text-xs px-2 py-1 rounded-full border ${getConfidenceColor(forecast.confidenceScore)}`}>
                              Độ tin cậy: {forecast.confidenceScore.toFixed(1)}%
                            </span>
                            <span className="text-xs text-muted-foreground">
                              {getMethodLabel(forecast.forecastMethod)}
                            </span>
                          </div>

                          {/* Recommendation */}
                          {forecast.insights?.recommendation && (
                            <div className="mt-3 p-3 bg-purple-50 border border-purple-100 rounded-lg">
                              <p className="text-sm text-purple-900">
                                <strong>💡 Khuyến nghị:</strong> {forecast.insights.recommendation}
                              </p>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex gap-2 ml-4">
                      <Button variant="outline" size="sm" onClick={() => handleView(forecast)}>
                        <Eye className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDelete(forecast)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
                {forecasts.length === 0 && (
                  <div className="text-center py-8 text-muted-foreground">
                    Chưa có dự báo nào. Nhấn &quot;Tạo Dự báo&quot; để bắt đầu.
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Trước
              </Button>
              <span className="text-sm text-muted-foreground">
                Trang {page + 1} / {totalPages}
              </span>
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                Sau
              </Button>
            </div>
          )}
        </>
      )}

      {/* Create Modal */}
      <EntityModal
        title="Tạo Dự báo Mới"
        open={viewMode === "create"}
        onClose={() => {
          setViewMode("list");
          reset();
        }}
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => {
                setViewMode("list");
                reset();
              }}
            >
              Hủy
            </Button>
            <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
              {isSubmitting ? "Đang tạo..." : "Tạo Dự báo"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Product ID *</label>
            <Input
              type="number"
              {...register("productId", { valueAsNumber: true })}
              className="mt-1"
              placeholder="Nhập ID sản phẩm"
            />
            {errors.productId && (
              <p className="text-sm text-destructive mt-1">{errors.productId.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Chu kỳ dự báo *</label>
            <select
              {...register("forecastPeriod")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="MONTHLY">Hàng tháng</option>
              <option value="QUARTERLY">Hàng quý</option>
              <option value="YEARLY">Hàng năm</option>
            </select>
            {errors.forecastPeriod && (
              <p className="text-sm text-destructive mt-1">{errors.forecastPeriod.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Ngày dự báo *</label>
            <Input type="date" {...register("forecastDate")} className="mt-1" />
            {errors.forecastDate && (
              <p className="text-sm text-destructive mt-1">{errors.forecastDate.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Phương pháp dự báo</label>
            <select
              {...register("forecastMethod")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="LINEAR_REGRESSION">Hồi quy tuyến tính</option>
              <option value="MOVING_AVERAGE">Trung bình trượt</option>
              <option value="EXPONENTIAL_SMOOTHING">Làm mượt hàm mũ</option>
            </select>
          </div>

          <div>
            <label className="text-sm font-medium">Số kỳ dự báo (1-24)</label>
            <Input
              type="number"
              {...register("numberOfPeriods", { valueAsNumber: true })}
              className="mt-1"
              min="1"
              max="24"
            />
            <p className="text-xs text-muted-foreground mt-1">
              Số kỳ trong tương lai cần dự báo
            </p>
          </div>

          <div>
            <label className="text-sm font-medium">Ghi chú</label>
            <textarea
              {...register("notes")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              placeholder="Ghi chú về dự báo này..."
            />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết Dự báo"
        open={viewMode === "detail" && selectedForecast !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedForecast(null);
        }}
        footer={
          selectedForecast && !selectedForecast.actualDemand && (
            <Button
              onClick={() => {
                const actual = prompt("Nhập nhu cầu thực tế:");
                if (actual && selectedForecast) {
                  handleUpdateActualDemand(selectedForecast.id, parseInt(actual));
                  setViewMode("list");
                }
              }}
            >
              Cập nhật Nhu cầu Thực tế
            </Button>
          )
        }
      >
        {selectedForecast && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Sản phẩm</label>
              <p className="text-lg font-semibold">{selectedForecast.productName}</p>
              <p className="text-sm text-muted-foreground">{selectedForecast.brandName}</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Chu kỳ</label>
                <p>{getPeriodLabel(selectedForecast.forecastPeriod)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ngày dự báo</label>
                <p>{formatDate(selectedForecast.forecastDate)}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Dự báo</label>
                <p className="text-2xl font-bold text-blue-600">
                  {selectedForecast.predictedDemand}
                </p>
              </div>
              {selectedForecast.actualDemand && (
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Thực tế</label>
                  <p className="text-2xl font-bold text-green-600">
                    {selectedForecast.actualDemand}
                  </p>
                </div>
              )}
            </div>

            <div>
              <label className="text-sm font-medium text-muted-foreground">Độ tin cậy</label>
              <div className="flex items-center gap-3 mt-1">
                <div className="flex-1 bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-gradient-to-r from-blue-500 to-purple-500 h-2 rounded-full"
                    style={{ width: `${selectedForecast.confidenceScore}%` }}
                  />
                </div>
                <span className="font-semibold">
                  {selectedForecast.confidenceScore.toFixed(1)}%
                </span>
              </div>
            </div>

            {selectedForecast.accuracy && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Độ chính xác</label>
                <p className={`text-xl font-bold ${getAccuracyColor(selectedForecast.accuracy)}`}>
                  {selectedForecast.accuracy}%
                </p>
              </div>
            )}

            <div>
              <label className="text-sm font-medium text-muted-foreground">Phương pháp</label>
              <p>{getMethodLabel(selectedForecast.forecastMethod)}</p>
            </div>

            {selectedForecast.insights && (
              <div className="space-y-3">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Xu hướng</label>
                  <div className="flex items-center gap-2 mt-1">
                    {getTrendIcon(selectedForecast.insights.trend)}
                    <span className="font-semibold">{selectedForecast.insights.trend}</span>
                    {selectedForecast.insights.trendPercentage > 0 && (
                      <span className="text-sm text-muted-foreground">
                        ({selectedForecast.insights.trendPercentage.toFixed(1)}%)
                      </span>
                    )}
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-muted-foreground">Các yếu tố ảnh hưởng</label>
                  <ul className="list-disc list-inside text-sm mt-1 space-y-1">
                    {selectedForecast.insights.influencingFactors.map((factor, idx) => (
                      <li key={idx}>{factor}</li>
                    ))}
                  </ul>
                </div>

                <div className="p-3 bg-purple-50 border border-purple-100 rounded-lg">
                  <label className="text-sm font-medium text-purple-900">💡 Khuyến nghị</label>
                  <p className="text-sm text-purple-900 mt-1">
                    {selectedForecast.insights.recommendation}
                  </p>
                </div>
              </div>
            )}

            {selectedForecast.notes && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ghi chú</label>
                <p className="text-sm">{selectedForecast.notes}</p>
              </div>
            )}

            <div className="text-xs text-muted-foreground pt-3 border-t">
              <p>Tạo bởi: {selectedForecast.createdByName || "System"}</p>
              <p>Ngày tạo: {formatDate(selectedForecast.createdAt)}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
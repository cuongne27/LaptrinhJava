"use client";

import { useState, useEffect } from "react";
import { apiClient } from "@/lib/api/client";
import { EntityModal } from "@/components/entity/EntityModal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import toast from "react-hot-toast";
import { formatCurrency, formatDate, getStatusColor } from "@/lib/utils";
import type { PaginatedResponse } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw } from "lucide-react";

interface Promotion {
  id: number;
  promotionName: string;
  promotionCode: string;
  discountType: string;
  discountValue: number;
  startDate: string;
  endDate: string;
  status: string;
  isActive: boolean;
  description?: string;
  createdAt: string;
}

const promotionSchema = z.object({
  promotionName: z.string().min(1, "Tên khuyến mãi không được để trống"),
  promotionCode: z.string().min(1, "Mã khuyến mãi không được để trống"),
  discountType: z.string().min(1, "Vui lòng chọn loại giảm giá"),
  discountValue: z.number().min(0.01, "Giá trị giảm giá phải lớn hơn 0"),
  startDate: z.string().min(1, "Vui lòng chọn ngày bắt đầu"),
  endDate: z.string().min(1, "Vui lòng chọn ngày kết thúc"),
  description: z.string().optional(),
});

type PromotionForm = z.infer<typeof promotionSchema>;

export default function PromotionsPage() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterByKeyword, setFilterByKeyword] = useState("");
  const [filterByCode, setFilterByCode] = useState("");
  const [filterByActive, setFilterByActive] = useState<boolean | null>(null);
  const [filterByTime, setFilterByTime] = useState<{ start?: string; end?: string }>({});
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedPromotion, setSelectedPromotion] = useState<Promotion | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<PromotionForm>({
    resolver: zodResolver(promotionSchema),
  });

  useEffect(() => {
    fetchPromotions();
  }, [page, search, filterByKeyword, filterByCode, filterByActive, filterByTime, refreshTrigger]);

  const fetchPromotions = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });

      // Build filter parameters based on active filters
      if (search) {
        params.append("searchKeyword", search);
      }
      
      if (filterByKeyword) {
        params.append("searchKeyword", filterByKeyword);
      }
      
      if (filterByCode) {
        params.append("searchKeyword", filterByCode);
      }
      
      if (filterByActive !== null) {
        // Map to status filter
        const status = filterByActive ? "ACTIVE" : "EXPIRED";
        params.append("status", status);
      }
      
      if (filterByTime.start) {
        params.append("fromDate", filterByTime.start);
      }
      
      if (filterByTime.end) {
        params.append("toDate", filterByTime.end);
      }

      const response = await apiClient.get<PaginatedResponse<Promotion>>(
        `/promotions?${params.toString()}`
      );
      
      setPromotions(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
    } catch (error: any) {
      console.error("Error fetching promotions:", error);
      toast.error(error.response?.data?.message || "Không thể tải danh sách khuyến mãi");
      setPromotions([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    setSearch("");
    setFilterByKeyword("");
    setFilterByCode("");
    setFilterByActive(null);
    setFilterByTime({});
    setPage(0);
    setRefreshTrigger(prev => prev + 1);
  };

  const handleFilterByCode = () => {
    if (!filterByCode.trim()) {
      toast.error("Vui lòng nhập mã khuyến mãi");
      return;
    }
    setPage(0);
    // No need to call fetchPromotions - useEffect will handle it
  };

  const clearFilters = () => {
    setFilterByKeyword("");
    setFilterByCode("");
    setFilterByActive(null);
    setFilterByTime({});
    setSearch("");
    setPage(0);
  };

  const handleCreate = () => {
    reset({
      promotionName: "",
      promotionCode: "",
      discountType: "",
      discountValue: 0,
      startDate: "",
      endDate: "",
    });
    setSelectedPromotion(null);
    setViewMode("create");
  };

  const handleEdit = (promotion: Promotion) => {
    reset({
      promotionName: promotion.promotionName,
      promotionCode: promotion.promotionCode,
      discountType: promotion.discountType,
      discountValue: promotion.discountValue,
      startDate: promotion.startDate,
      endDate: promotion.endDate,
      description: promotion.description,
    });
    setSelectedPromotion(promotion);
    setViewMode("edit");
  };

  const handleView = (promotion: Promotion) => {
    setSelectedPromotion(promotion);
    setViewMode("detail");
  };

  const handleDelete = async (promotion: Promotion) => {
    if (!confirm(`Bạn có chắc muốn xóa khuyến mãi "${promotion.promotionName}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/promotions/${promotion.id}`);
      toast.success("Xóa thành công!");
      fetchPromotions();
    } catch (error: any) {
      console.error("Error deleting promotion:", error);
      const errorMessage = error.response?.data?.message || error.response?.data?.error || "Không thể xóa khuyến mãi";
      toast.error(errorMessage);
    }
  };

  const onSubmit = async (data: PromotionForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/promotions", data);
        toast.success("Tạo khuyến mãi thành công!");
      } else if (viewMode === "edit" && selectedPromotion) {
        await apiClient.put(`/promotions/${selectedPromotion.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchPromotions();
    } catch (error: any) {
      console.error("Error saving promotion:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu khuyến mãi";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Khuyến mãi</h1>
          <p className="text-muted-foreground">Quản lý chương trình khuyến mãi</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleRefresh} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Làm mới
          </Button>
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm mới
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Tìm kiếm & Lọc</CardTitle>
          <CardDescription>Tìm kiếm và lọc khuyến mãi theo các tiêu chí</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm khuyến mãi..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
                clearFilters();
              }}
              className="pl-9"
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Tìm theo Keyword</label>
              <div className="flex gap-2">
                <Input
                  placeholder="Nhập keyword..."
                  value={filterByKeyword}
                  onChange={(e) => {
                    setFilterByKeyword(e.target.value);
                    setSearch("");
                    setFilterByCode("");
                    setFilterByActive(null);
                    setFilterByTime({});
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      setPage(0);
                      fetchPromotions();
                    }
                  }}
                />
                <Button
                  onClick={() => {
                    setPage(0);
                  }}
                  variant="outline"
                  size="sm"
                >
                  Tìm
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Tìm theo Mã khuyến mãi</label>
              <div className="flex gap-2">
                <Input
                  placeholder="Nhập mã..."
                  value={filterByCode}
                  onChange={(e) => {
                    setFilterByCode(e.target.value);
                    setSearch("");
                    setFilterByKeyword("");
                    setFilterByActive(null);
                    setFilterByTime({});
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleFilterByCode();
                    }
                  }}
                />
                <Button onClick={handleFilterByCode} variant="outline" size="sm">
                  Tìm
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc theo Trạng thái</label>
              <div className="flex gap-2">
                <select
                  value={filterByActive === null ? "" : filterByActive.toString()}
                  onChange={(e) => {
                    const value = e.target.value === "" ? null : e.target.value === "true";
                    setFilterByActive(value);
                    setSearch("");
                    setFilterByKeyword("");
                    setFilterByCode("");
                    setFilterByTime({});
                    setPage(0);
                  }}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="">Tất cả</option>
                  <option value="true">Đang hoạt động</option>
                  <option value="false">Không hoạt động</option>
                </select>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc theo Thời gian - Từ ngày</label>
              <Input
                type="date"
                value={filterByTime.start || ""}
                onChange={(e) => {
                  setFilterByTime({ ...filterByTime, start: e.target.value });
                  setSearch("");
                  setFilterByKeyword("");
                  setFilterByCode("");
                  setFilterByActive(null);
                }}
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc theo Thời gian - Đến ngày</label>
              <Input
                type="date"
                value={filterByTime.end || ""}
                onChange={(e) => {
                  setFilterByTime({ ...filterByTime, end: e.target.value });
                  setSearch("");
                  setFilterByKeyword("");
                  setFilterByCode("");
                  setFilterByActive(null);
                }}
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc nhanh</label>
              <Button
                onClick={() => {
                  setFilterByTime({});
                  setFilterByKeyword("");
                  setFilterByCode("");
                  setFilterByActive(true);
                  setSearch("");
                  setPage(0);
                }}
                variant="outline"
                className="w-full"
              >
                Chỉ hiện đang hoạt động
              </Button>
            </div>
          </div>
          {(filterByKeyword || filterByCode || filterByActive !== null || filterByTime.start || filterByTime.end) && (
            <Button variant="ghost" size="sm" onClick={clearFilters} className="w-full">
              Xóa bộ lọc
            </Button>
          )}
        </CardContent>
      </Card>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Đang tải...</div>
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {promotions.map((promotion) => (
              <Card key={promotion.id} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{promotion.promotionName}</CardTitle>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(promotion.status)}`}>
                      {promotion.status}
                    </span>
                  </div>
                  <CardDescription>
                    {promotion.promotionCode} - {formatDate(promotion.startDate)} đến {formatDate(promotion.endDate)}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Loại</span>
                      <span className="font-medium">{promotion.discountType}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Giá trị</span>
                      <span className="font-bold text-primary">
                        {formatCurrency(promotion.discountValue)}
                      </span>
                    </div>
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <Button variant="outline" size="sm" onClick={() => handleView(promotion)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleEdit(promotion)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(promotion)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

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

      {/* Create/Edit Modal */}
      <EntityModal
        title={viewMode === "create" ? "Thêm khuyến mãi mới" : "Sửa khuyến mãi"}
        open={viewMode === "create" || viewMode === "edit"}
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
            <Button
              type="button"
              onClick={async () => {
                const isValid = await trigger();
                if (isValid) {
                  onSubmit(getValues());
                } else {
                  toast.error("Vui lòng điền đầy đủ thông tin bắt buộc");
                }
              }}
              disabled={isSubmitting}
            >
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <form className="space-y-4">
          <div>
            <label className="text-sm font-medium">Tên khuyến mãi *</label>
            <Input {...register("promotionName")} className="mt-1" />
            {errors.promotionName && (
              <p className="text-sm text-destructive mt-1">{errors.promotionName.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Mã khuyến mãi *</label>
            <Input {...register("promotionCode")} className="mt-1" />
            {errors.promotionCode && (
              <p className="text-sm text-destructive mt-1">{errors.promotionCode.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Loại giảm giá *</label>
            <select
              {...register("discountType")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn loại</option>
              <option value="PERCENTAGE">Phần trăm</option>
              <option value="FIXED_AMOUNT">Số tiền cố định</option>
            </select>
            {errors.discountType && (
              <p className="text-sm text-destructive mt-1">{errors.discountType.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Giá trị giảm giá *</label>
            <Input
              type="number"
              step="0.01"
              {...register("discountValue", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.discountValue && (
              <p className="text-sm text-destructive mt-1">{errors.discountValue.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Ngày bắt đầu *</label>
            <Input type="date" {...register("startDate")} className="mt-1" />
            {errors.startDate && (
              <p className="text-sm text-destructive mt-1">{errors.startDate.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Ngày kết thúc *</label>
            <Input type="date" {...register("endDate")} className="mt-1" />
            {errors.endDate && (
              <p className="text-sm text-destructive mt-1">{errors.endDate.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Mô tả</label>
            <textarea
              {...register("description")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết khuyến mãi"
        open={viewMode === "detail" && selectedPromotion !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedPromotion(null);
        }}
        footer={
          <Button onClick={() => selectedPromotion && handleEdit(selectedPromotion)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedPromotion && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tên khuyến mãi</label>
              <p className="text-lg font-semibold">{selectedPromotion.promotionName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Mã khuyến mãi</label>
              <p>{selectedPromotion.promotionCode}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedPromotion.status)}`}>
                  {selectedPromotion.status}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Loại giảm giá</label>
              <p>{selectedPromotion.discountType}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Giá trị</label>
              <p className="text-2xl font-bold text-primary">
                {formatCurrency(selectedPromotion.discountValue)}
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Thời gian</label>
              <p>{formatDate(selectedPromotion.startDate)} - {formatDate(selectedPromotion.endDate)}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
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
import type { Brand, PaginatedResponse } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw } from "lucide-react";

const brandSchema = z.object({
  brandName: z.string().min(1, "Tên thương hiệu không được để trống"),
  headquartersAddress: z.string().optional(),
  taxCode: z.string().optional(),
  contactInfo: z.string().optional(),
});

type BrandForm = z.infer<typeof brandSchema>;

export default function BrandsPage() {
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedBrand, setSelectedBrand] = useState<Brand | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<BrandForm>({
    resolver: zodResolver(brandSchema),
  });

  useEffect(() => {
    fetchBrands();
  }, [page, search]);

  const fetchBrands = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      const response = await apiClient.get<PaginatedResponse<Brand>>(
        `/brands?${params.toString()}`
      );
      setBrands(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching brands:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    reset();
    setSelectedBrand(null);
    setViewMode("create");
  };

  const handleEdit = (brand: Brand) => {
    reset({
      brandName: brand.brandName,
      headquartersAddress: brand.headquartersAddress || "",
      taxCode: brand.taxCode || "",
      contactInfo: brand.contactInfo || "",
    });
    setSelectedBrand(brand);
    setViewMode("edit");
  };

  const handleView = (brand: Brand) => {
    setSelectedBrand(brand);
    setViewMode("detail");
  };

  const handleDelete = async (brand: Brand) => {
    if (!confirm(`Bạn có chắc muốn xóa thương hiệu "${brand.brandName}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/brands/delete/${brand.id}`);
      toast.success("Xóa thành công!");
      fetchBrands();
    } catch (error) {
      console.error("Error deleting brand:", error);
    }
  };

  const onSubmit = async (data: BrandForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/brands/create", data);
        toast.success("Tạo thương hiệu thành công!");
      } else if (viewMode === "edit" && selectedBrand) {
        await apiClient.put(`/brands/update/${selectedBrand.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchBrands();
    } catch (error) {
      console.error("Error saving brand:", error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Thương hiệu</h1>
          <p className="text-muted-foreground">Quản lý thương hiệu xe điện</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchBrands} disabled={loading}>
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
          <CardTitle>Tìm kiếm</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm thương hiệu..."
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
              <CardTitle>Danh sách ({brands.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {brands.map((brand) => (
                  <div
                    key={brand.id}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex-1">
                      <h3 className="font-semibold text-lg">{brand.brandName}</h3>
                      <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                        {brand.headquartersAddress && (
                          <>
                            <span>{brand.headquartersAddress}</span>
                            <span>•</span>
                          </>
                        )}
                        {brand.taxCode && (
                          <>
                            <span>MST: {brand.taxCode}</span>
                            <span>•</span>
                          </>
                        )}
                        {brand.totalDealers !== undefined && (
                          <span>{brand.totalDealers} đại lý</span>
                        )}
                        {brand.totalProducts !== undefined && (
                          <>
                            <span>•</span>
                            <span>{brand.totalProducts} sản phẩm</span>
                          </>
                        )}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" onClick={() => handleView(brand)}>
                        <Eye className="mr-2 h-4 w-4" />
                        Xem
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleEdit(brand)}>
                        <Edit className="mr-2 h-4 w-4" />
                        Sửa
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDelete(brand)}
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Xóa
                      </Button>
                    </div>
                  </div>
                ))}
                {brands.length === 0 && (
                  <div className="text-center py-8 text-muted-foreground">
                    Không có dữ liệu
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

      {/* Create/Edit Modal */}
      <EntityModal
        title={viewMode === "create" ? "Thêm thương hiệu mới" : "Sửa thương hiệu"}
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
            <Button onClick={handleSubmit(onSubmit)} disabled={isSubmitting}>
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Tên thương hiệu *</label>
            <Input {...register("brandName")} className="mt-1" />
            {errors.brandName && (
              <p className="text-sm text-destructive mt-1">{errors.brandName.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Địa chỉ trụ sở</label>
            <Input {...register("headquartersAddress")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Mã số thuế</label>
            <Input {...register("taxCode")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Thông tin liên hệ</label>
            <Input {...register("contactInfo")} className="mt-1" />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết thương hiệu"
        open={viewMode === "detail" && selectedBrand !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedBrand(null);
        }}
        footer={
          <Button
            onClick={() => {
              if (selectedBrand) handleEdit(selectedBrand);
            }}
          >
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedBrand && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tên thương hiệu</label>
              <p className="text-lg font-semibold">{selectedBrand.brandName}</p>
            </div>
            {selectedBrand.headquartersAddress && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">
                  Địa chỉ trụ sở
                </label>
                <p>{selectedBrand.headquartersAddress}</p>
              </div>
            )}
            {selectedBrand.taxCode && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Mã số thuế</label>
                <p>{selectedBrand.taxCode}</p>
              </div>
            )}
            {selectedBrand.contactInfo && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">
                  Thông tin liên hệ
                </label>
                <p>{selectedBrand.contactInfo}</p>
              </div>
            )}
            <div className="grid grid-cols-2 gap-4 pt-4 border-t">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Số đại lý</label>
                <p className="text-2xl font-bold">{selectedBrand.totalDealers || 0}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Số sản phẩm</label>
                <p className="text-2xl font-bold">{selectedBrand.totalProducts || 0}</p>
              </div>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}

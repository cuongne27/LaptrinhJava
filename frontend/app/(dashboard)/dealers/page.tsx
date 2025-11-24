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
import type { Dealer, PaginatedResponse, Brand } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Building2 } from "lucide-react";

const dealerSchema = z.object({
  dealerName: z.string().min(1, "Tên đại lý không được để trống"),
  address: z.string().optional(),
  phoneNumber: z.string().optional(),
  email: z.string().email("Email không hợp lệ").optional().or(z.literal("")),
  dealerLevel: z.string().optional(),
  brandId: z.number().min(1, "Vui lòng chọn thương hiệu"),
});

type DealerForm = z.infer<typeof dealerSchema>;

export default function DealersPage() {
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedDealer, setSelectedDealer] = useState<Dealer | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<DealerForm>({
    resolver: zodResolver(dealerSchema),
  });

  useEffect(() => {
    fetchDealers();
    fetchBrands();
  }, [page, search]);

  const fetchDealers = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<Dealer>>(
        `/dealers/filter?${params.toString()}`
      );
      setDealers(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching dealers:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchBrands = async () => {
    try {
      const response = await apiClient.get<Brand[]>("/brands/all");
      setBrands(response.data);
    } catch (error) {
      console.error("Error fetching brands:", error);
    }
  };

  const handleCreate = () => {
    reset({
      dealerName: "",
      address: "",
      phoneNumber: "",
      email: "",
      dealerLevel: "",
      brandId: brands[0]?.id || 0,
    });
    setSelectedDealer(null);
    setViewMode("create");
  };

  const handleEdit = (dealer: Dealer) => {
    reset({
      dealerName: dealer.dealerName,
      address: dealer.address || "",
      phoneNumber: dealer.phoneNumber || "",
      email: dealer.email || "",
      dealerLevel: dealer.dealerLevel || "",
      brandId: dealer.brandId,
    });
    setSelectedDealer(dealer);
    setViewMode("edit");
  };

  const handleView = (dealer: Dealer) => {
    setSelectedDealer(dealer);
    setViewMode("detail");
  };

  const handleDelete = async (dealer: Dealer) => {
    if (!confirm(`Bạn có chắc muốn xóa đại lý "${dealer.dealerName}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/dealers/delete/${dealer.id}`);
      toast.success("Xóa thành công!");
      fetchDealers();
    } catch (error) {
      console.error("Error deleting dealer:", error);
    }
  };

  const onSubmit = async (data: DealerForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/dealers/create", data);
        toast.success("Tạo đại lý thành công!");
      } else if (viewMode === "edit" && selectedDealer) {
        await apiClient.put(`/dealers/update/${selectedDealer.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchDealers();
    } catch (error) {
      console.error("Error saving dealer:", error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Đại lý</h1>
          <p className="text-muted-foreground">Quản lý đại lý bán hàng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchDealers} disabled={loading}>
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
              placeholder="Tìm kiếm đại lý..."
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
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {dealers.map((dealer) => (
              <Card key={dealer.id}>
                <CardHeader>
                  <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
                      <Building2 className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <CardTitle className="text-lg">{dealer.dealerName}</CardTitle>
                      <CardDescription>{dealer.brandName}</CardDescription>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    {dealer.address && (
                      <p className="text-sm text-muted-foreground">{dealer.address}</p>
                    )}
                    {dealer.phoneNumber && (
                      <p className="text-sm text-muted-foreground">{dealer.phoneNumber}</p>
                    )}
                    {dealer.email && (
                      <p className="text-sm text-muted-foreground">{dealer.email}</p>
                    )}
                    {dealer.dealerLevel && (
                      <span className="inline-block px-2 py-1 text-xs bg-secondary rounded">
                        {dealer.dealerLevel}
                      </span>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => handleView(dealer)} className="flex-1">
                      <Eye className="mr-2 h-4 w-4" />
                      Xem
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleEdit(dealer)} className="flex-1">
                      <Edit className="mr-2 h-4 w-4" />
                      Sửa
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(dealer)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {dealers.length === 0 && (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                Không có dữ liệu
              </CardContent>
            </Card>
          )}

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
        title={viewMode === "create" ? "Thêm đại lý mới" : "Sửa đại lý"}
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
            <label className="text-sm font-medium">Tên đại lý *</label>
            <Input {...register("dealerName")} className="mt-1" />
            {errors.dealerName && (
              <p className="text-sm text-destructive mt-1">{errors.dealerName.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Thương hiệu *</label>
            <select
              {...register("brandId", { valueAsNumber: true })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn thương hiệu</option>
              {brands.map((brand) => (
                <option key={brand.id} value={brand.id}>
                  {brand.brandName}
                </option>
              ))}
            </select>
            {errors.brandId && (
              <p className="text-sm text-destructive mt-1">{errors.brandId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Địa chỉ</label>
            <Input {...register("address")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Số điện thoại</label>
            <Input {...register("phoneNumber")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Email</label>
            <Input type="email" {...register("email")} className="mt-1" />
            {errors.email && (
              <p className="text-sm text-destructive mt-1">{errors.email.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Cấp độ đại lý</label>
            <select
              {...register("dealerLevel")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn cấp độ</option>
              <option value="Gold">Gold</option>
              <option value="Silver">Silver</option>
              <option value="Bronze">Bronze</option>
              <option value="Platinum">Platinum</option>
            </select>
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết đại lý"
        open={viewMode === "detail" && selectedDealer !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedDealer(null);
        }}
        footer={
          <Button onClick={() => selectedDealer && handleEdit(selectedDealer)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedDealer && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tên đại lý</label>
              <p className="text-lg font-semibold">{selectedDealer.dealerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Thương hiệu</label>
              <p>{selectedDealer.brandName}</p>
            </div>
            {selectedDealer.address && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Địa chỉ</label>
                <p>{selectedDealer.address}</p>
              </div>
            )}
            {selectedDealer.phoneNumber && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Số điện thoại</label>
                <p>{selectedDealer.phoneNumber}</p>
              </div>
            )}
            {selectedDealer.email && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Email</label>
                <p>{selectedDealer.email}</p>
              </div>
            )}
            {selectedDealer.dealerLevel && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Cấp độ</label>
                <span className="inline-block px-2 py-1 text-xs bg-secondary rounded">
                  {selectedDealer.dealerLevel}
                </span>
              </div>
            )}
          </div>
        )}
      </EntityModal>
    </div>
  );
}

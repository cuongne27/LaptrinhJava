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
import { formatDate, getStatusColor } from "@/lib/utils";
import type { VehicleListResponse, PaginatedResponse } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Truck } from "lucide-react";

interface Product {
  id: number;
  name: string;
}

interface Dealer {
  id: number;
  name: string;
}

const vehicleSchema = z.object({
  id: z.string().min(1, "Vehicle ID không được để trống"),
  vin: z.string().min(1, "VIN không được để trống"),
  batterySerial: z.string().optional(),
  color: z.string().optional(),
  manufactureDate: z.string().optional(),
  status: z.string().optional(),
  productId: z.number().min(1, "Vui lòng chọn sản phẩm"),
  dealerId: z.number().min(1, "Vui lòng chọn đại lý"),
});

type VehicleForm = z.infer<typeof vehicleSchema>;

export default function VehiclesPage() {
  const [vehicles, setVehicles] = useState<VehicleListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterByVin, setFilterByVin] = useState("");
  const [filterByDealer, setFilterByDealer] = useState("");
  const [filterByProduct, setFilterByProduct] = useState("");
  const [showAvailableOnly, setShowAvailableOnly] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedVehicle, setSelectedVehicle] = useState<VehicleListResponse | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");
  
  // New states for dropdowns
  const [products, setProducts] = useState<Product[]>([]);
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [loadingDropdowns, setLoadingDropdowns] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<VehicleForm>({
    resolver: zodResolver(vehicleSchema),
  });

  useEffect(() => {
    fetchVehicles();
  }, [page, search, filterByVin, filterByDealer, filterByProduct, showAvailableOnly]);

  // Fetch products and dealers when opening create/edit modal
  useEffect(() => {
    if (viewMode === "create" || viewMode === "edit") {
      fetchProductsAndDealers();
    }
  }, [viewMode]);

  const fetchProductsAndDealers = async () => {
    try {
      setLoadingDropdowns(true);
      
      // Fetch products - Using correct API endpoint from backend
      try {
        const productsRes = await apiClient.get("/products?page=0&size=100");
        const productsData = productsRes.data;
        
        // Backend returns Page<ProductListResponse> with content array
        if (productsData.content && Array.isArray(productsData.content)) {
          // Map to our interface format
          const mappedProducts = productsData.content.map((p: any) => ({
            id: p.id,
            name: p.productName // Backend uses "productName" field
          }));
          setProducts(mappedProducts);
        } else {
          setProducts([]);
        }
      } catch (error: any) {
        console.error("Error fetching products:", error);
        // Don't show error toast, just use fallback to input
        setProducts([]);
      }
      
      // Fetch dealers - Using correct API endpoint from backend
      try {
        const dealersRes = await apiClient.get("/dealers/filter?page=0&size=100");
        const dealersData = dealersRes.data;
        
        // Backend returns Page<DealerListResponse> with content array
        if (dealersData.content && Array.isArray(dealersData.content)) {
          // Map to our interface format
          const mappedDealers = dealersData.content.map((d: any) => ({
            id: d.id,
            name: d.dealerName // Backend uses "dealerName" field
          }));
          setDealers(mappedDealers);
        } else {
          setDealers([]);
        }
      } catch (error: any) {
        console.error("Error fetching dealers:", error);
        // Don't show error toast, just use fallback to input
        setDealers([]);
      }
      
    } catch (error) {
      console.error("Error fetching dropdowns:", error);
      // Only show error if both fail
    } finally {
      setLoadingDropdowns(false);
    }
  };

  const fetchVehicles = async () => {
    try {
      setLoading(true);
      let url = `/vehicles`;
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });

      if (filterByVin) {
        url = `/vehicles/by-vin?vin=${encodeURIComponent(filterByVin)}`;
      } else if (showAvailableOnly) {
        url = `/vehicles/available`;
      } else if (filterByDealer) {
        url = `/vehicles/by-dealer?dealerId=${filterByDealer}`;
      } else if (filterByProduct) {
        url = `/vehicles/by-product?productId=${filterByProduct}`;
      } else if (search) {
        params.append("searchKeyword", search);
      }

      if (!filterByVin && !showAvailableOnly && !filterByDealer && !filterByProduct) {
        url += `?${params.toString()}`;
      } else if (filterByVin || showAvailableOnly || filterByDealer || filterByProduct) {
        url += url.includes("?") ? `&${params.toString()}` : `?${params.toString()}`;
      }

      const response = await apiClient.get<PaginatedResponse<VehicleListResponse> | VehicleListResponse>(
        url
      );
      
      // Handle single vehicle response (by VIN)
      if (filterByVin && !Array.isArray(response.data)) {
        setVehicles([response.data as VehicleListResponse]);
        setTotalPages(1);
      } else {
        const data = Array.isArray(response.data) ? response.data : (response.data as any).content || [];
        setVehicles(data);
        setTotalPages(Array.isArray(response.data) ? 1 : (response.data as any).totalPages || 1);
      }
    } catch (error: any) {
      console.error("Error fetching vehicles:", error);
      toast.error(error.response?.data?.message || "Không thể tải danh sách xe");
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterByVin = () => {
    if (!filterByVin.trim()) {
      toast.error("Vui lòng nhập VIN");
      return;
    }
    setPage(0);
    fetchVehicles();
  };

  const clearFilters = () => {
    setFilterByVin("");
    setFilterByDealer("");
    setFilterByProduct("");
    setShowAvailableOnly(false);
    setSearch("");
    setPage(0);
  };

  const handleCreate = () => {
    reset({
      id: "",
      vin: "",
      batterySerial: "",
      color: "",
      manufactureDate: "",
      status: "AVAILABLE",
      productId: 0,
      dealerId: 0,
    });
    setSelectedVehicle(null);
    setViewMode("create");
  };

  const handleEdit = (vehicle: VehicleListResponse) => {
    reset({
      id: vehicle.id,
      vin: vehicle.vin,
      batterySerial: "",
      color: vehicle.color || "",
      manufactureDate: vehicle.manufactureDate || "",
      status: vehicle.status,
      productId: vehicle.productId,
      dealerId: vehicle.dealerId,
    });
    setSelectedVehicle(vehicle);
    setViewMode("edit");
  };

  const handleView = (vehicle: VehicleListResponse) => {
    setSelectedVehicle(vehicle);
    setViewMode("detail");
  };

  const handleDelete = async (vehicle: VehicleListResponse) => {
    if (!confirm(`Bạn có chắc muốn xóa xe "${vehicle.id}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/vehicles/${vehicle.id}`);
      toast.success("Xóa thành công!");
      fetchVehicles();
    } catch (error) {
      console.error("Error deleting vehicle:", error);
    }
  };

  const onSubmit = async (data: VehicleForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/vehicles", data);
        toast.success("Tạo xe thành công!");
      } else if (viewMode === "edit" && selectedVehicle) {
        await apiClient.put(`/vehicles/${selectedVehicle.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchVehicles();
    } catch (error: any) {
      console.error("Error saving vehicle:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu xe";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Xe</h1>
          <p className="text-muted-foreground">Quản lý xe trong kho</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchVehicles} disabled={loading}>
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
          <CardDescription>Tìm kiếm và lọc xe theo các tiêu chí</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm theo ID, VIN..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
                clearFilters();
              }}
              className="pl-9"
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Tìm theo VIN</label>
              <div className="flex gap-2">
                <Input
                  placeholder="Nhập VIN..."
                  value={filterByVin}
                  onChange={(e) => {
                    setFilterByVin(e.target.value);
                    setSearch("");
                    setFilterByDealer("");
                    setFilterByProduct("");
                    setShowAvailableOnly(false);
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleFilterByVin();
                    }
                  }}
                />
                <Button onClick={handleFilterByVin} variant="outline" size="sm">
                  Tìm
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc theo Đại lý</label>
              <div className="flex gap-2">
                <Input
                  type="number"
                  placeholder="Dealer ID..."
                  value={filterByDealer}
                  onChange={(e) => {
                    setFilterByDealer(e.target.value);
                    setSearch("");
                    setFilterByVin("");
                    setFilterByProduct("");
                    setShowAvailableOnly(false);
                  }}
                />
                <Button
                  onClick={() => {
                    setPage(0);
                    fetchVehicles();
                  }}
                  variant="outline"
                  size="sm"
                >
                  Lọc
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc theo Sản phẩm</label>
              <div className="flex gap-2">
                <Input
                  type="number"
                  placeholder="Product ID..."
                  value={filterByProduct}
                  onChange={(e) => {
                    setFilterByProduct(e.target.value);
                    setSearch("");
                    setFilterByVin("");
                    setFilterByDealer("");
                    setShowAvailableOnly(false);
                  }}
                />
                <Button
                  onClick={() => {
                    setPage(0);
                    fetchVehicles();
                  }}
                  variant="outline"
                  size="sm"
                >
                  Lọc
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Lọc nhanh</label>
              <Button
                variant={showAvailableOnly ? "default" : "outline"}
                onClick={() => {
                  setShowAvailableOnly(!showAvailableOnly);
                  setSearch("");
                  setFilterByVin("");
                  setFilterByDealer("");
                  setFilterByProduct("");
                  setPage(0);
                }}
                className="w-full"
              >
                {showAvailableOnly ? "✓ Chỉ hiện có sẵn" : "Chỉ hiện có sẵn"}
              </Button>
            </div>
          </div>
          {(filterByVin || filterByDealer || filterByProduct || showAvailableOnly) && (
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
          <Card>
            <CardHeader>
              <CardTitle>Danh sách xe ({vehicles.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {vehicles.map((vehicle) => (
                  <div
                    key={vehicle.id}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
                          <Truck className="h-6 w-6 text-primary" />
                        </div>
                        <div>
                          <div className="flex items-center gap-3">
                            <h3 className="font-semibold">{vehicle.productName}</h3>
                            <span
                              className={`px-2 py-1 text-xs rounded-full ${getStatusColor(
                                vehicle.status
                              )}`}
                            >
                              {vehicle.status}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                            <span>ID: {vehicle.id}</span>
                            <span>•</span>
                            <span>VIN: {vehicle.vin}</span>
                            {vehicle.color && (
                              <>
                                <span>•</span>
                                <span>Màu: {vehicle.color}</span>
                              </>
                            )}
                            {vehicle.dealerName && (
                              <>
                                <span>•</span>
                                <span>Đại lý: {vehicle.dealerName}</span>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" onClick={() => handleView(vehicle)}>
                        <Eye className="mr-2 h-4 w-4" />
                        Xem
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleEdit(vehicle)}>
                        <Edit className="mr-2 h-4 w-4" />
                        Sửa
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDelete(vehicle)}
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Xóa
                      </Button>
                    </div>
                  </div>
                ))}
                {vehicles.length === 0 && (
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
        title={viewMode === "create" ? "Thêm xe mới" : "Sửa xe"}
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
              onClick={handleSubmit(onSubmit)}
              disabled={isSubmitting}
            >
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Vehicle ID *</label>
            <Input {...register("id")} className="mt-1" />
            {errors.id && (
              <p className="text-sm text-destructive mt-1">{errors.id.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">VIN *</label>
            <Input {...register("vin")} className="mt-1" />
            {errors.vin && (
              <p className="text-sm text-destructive mt-1">{errors.vin.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Battery Serial</label>
            <Input {...register("batterySerial")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Màu xe</label>
            <Input {...register("color")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Ngày sản xuất</label>
            <Input type="date" {...register("manufactureDate")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Trạng thái</label>
            <select
              {...register("status")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="AVAILABLE">AVAILABLE</option>
              <option value="SOLD">SOLD</option>
              <option value="RESERVED">RESERVED</option>
              <option value="IN_TRANSIT">IN_TRANSIT</option>
              <option value="DAMAGED">DAMAGED</option>
            </select>
          </div>
          <div>
            <label className="text-sm font-medium">Sản phẩm *</label>
            {products.length > 0 ? (
              <select
                {...register("productId", { valueAsNumber: true })}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
                disabled={loadingDropdowns}
              >
                <option value={0}>-- Chọn sản phẩm --</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} (ID: {product.id})
                  </option>
                ))}
              </select>
            ) : (
              <Input
                type="number"
                {...register("productId", { valueAsNumber: true })}
                className="mt-1"
                placeholder="Nhập Product ID"
              />
            )}
            {errors.productId && (
              <p className="text-sm text-destructive mt-1">{errors.productId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Đại lý *</label>
            {dealers.length > 0 ? (
              <select
                {...register("dealerId", { valueAsNumber: true })}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
                disabled={loadingDropdowns}
              >
                <option value={0}>-- Chọn đại lý --</option>
                {dealers.map((dealer) => (
                  <option key={dealer.id} value={dealer.id}>
                    {dealer.name} (ID: {dealer.id})
                  </option>
                ))}
              </select>
            ) : (
              <Input
                type="number"
                {...register("dealerId", { valueAsNumber: true })}
                className="mt-1"
                placeholder="Nhập Dealer ID"
              />
            )}
            {errors.dealerId && (
              <p className="text-sm text-destructive mt-1">{errors.dealerId.message}</p>
            )}
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết xe"
        open={viewMode === "detail" && selectedVehicle !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedVehicle(null);
        }}
        footer={
          <Button onClick={() => selectedVehicle && handleEdit(selectedVehicle)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedVehicle && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Vehicle ID</label>
              <p className="text-lg font-semibold">{selectedVehicle.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">VIN</label>
              <p>{selectedVehicle.vin}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Sản phẩm</label>
              <p>{selectedVehicle.productName}</p>
            </div>
            {selectedVehicle.color && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Màu</label>
                <p>{selectedVehicle.color}</p>
              </div>
            )}
            {selectedVehicle.manufactureDate && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ngày sản xuất</label>
                <p>{formatDate(selectedVehicle.manufactureDate)}</p>
              </div>
            )}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <span
                className={`inline-block px-2 py-1 text-xs rounded-full ${getStatusColor(
                  selectedVehicle.status
                )}`}
              >
                {selectedVehicle.status}
              </span>
            </div>
            {selectedVehicle.dealerName && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Đại lý</label>
                <p>{selectedVehicle.dealerName}</p>
              </div>
            )}
          </div>
        )}
      </EntityModal>
    </div>
  );
}
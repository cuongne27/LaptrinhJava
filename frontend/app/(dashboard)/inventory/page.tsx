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
import type { Inventory, PaginatedResponse, Product, Dealer } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, AlertTriangle, Package } from "lucide-react";

const inventorySchema = z.object({
  productId: z.preprocess(
    (val) => (val === "" || val === "0" || val === 0 ? undefined : Number(val)),
    z.number().min(1, "Vui lòng chọn sản phẩm")
  ),
  dealerId: z.preprocess(
    (val) => (val === "" || val === "0" || val === 0 || val === null ? null : Number(val)),
    z.number().nullable().optional()
  ),
  totalQuantity: z.preprocess(
    (val) => (val === "" ? 0 : Number(val)),
    z.number().min(0)
  ),
  reservedQuantity: z.preprocess(
    (val) => (val === "" ? 0 : Number(val)),
    z.number().min(0)
  ),
  availableQuantity: z.preprocess(
    (val) => (val === "" ? 0 : Number(val)),
    z.number().min(0)
  ),
  inTransitQuantity: z.preprocess(
    (val) => (val === "" ? 0 : Number(val)),
    z.number().min(0)
  ),
  location: z.string().optional(),
});

type InventoryForm = z.infer<typeof inventorySchema>;

export default function InventoryPage() {
  const [inventory, setInventory] = useState<Inventory[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingDropdowns, setLoadingDropdowns] = useState(false);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedInventory, setSelectedInventory] = useState<Inventory | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<InventoryForm>({
    resolver: zodResolver(inventorySchema),
  });

  useEffect(() => {
    fetchInventory();
  }, [page, search, refreshTrigger]);

  // Fetch products and dealers when opening create/edit modal
  useEffect(() => {
    if (viewMode === "create" || viewMode === "edit") {
      fetchProductsAndDealers();
    }
  }, [viewMode]);

  const fetchProductsAndDealers = async () => {
    try {
      setLoadingDropdowns(true);
      
      // Fetch products
      try {
        const response = await apiClient.get<PaginatedResponse<Product>>("/products?page=0&size=1000");
        console.log("Products API response:", response.data);
        const productsData = response.data;
        
        if (productsData.content && Array.isArray(productsData.content)) {
          console.log("Setting products from content:", productsData.content);
          setProducts(productsData.content);
        } else if (Array.isArray(productsData)) {
          console.log("Setting products from array:", productsData);
          setProducts(productsData);
        } else {
          console.log("No valid products data");
          setProducts([]);
        }
      } catch (error: any) {
        console.error("Error fetching products:", error);
        setProducts([]);
      }

      // Fetch dealers
      try {
        const dealersRes = await apiClient.get("/dealers/filter?page=0&size=100");
        console.log("Dealers API response:", dealersRes.data);
        const dealersData = dealersRes.data;
        
        if (dealersData.content && Array.isArray(dealersData.content)) {
          console.log("Setting dealers from content:", dealersData.content);
          setDealers(dealersData.content);
        } else if (Array.isArray(dealersData)) {
          console.log("Setting dealers from array:", dealersData);
          setDealers(dealersData);
        } else {
          console.log("No valid dealers data");
          setDealers([]);
        }
      } catch (error: any) {
        console.error("Error fetching dealers:", error);
        setDealers([]);
      }

    } catch (error) {
      console.error("Error fetching dropdowns:", error);
      toast.error("Không thể tải danh sách sản phẩm và đại lý");
    } finally {
      setLoadingDropdowns(false);
    }
  };

  const fetchInventory = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<Inventory>>(
        `/inventory?${params.toString()}`
      );
      setInventory(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error: any) {
      console.error("Error fetching inventory:", error);
      toast.error(error.response?.data?.message || "Không thể tải danh sách tồn kho");
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    setSearch("");
    setPage(0);
    setRefreshTrigger(prev => prev + 1);
  };

  const handleCreate = () => {
    setSelectedInventory(null);
    setViewMode("create");
    // Reset form after modal opens
    setTimeout(() => {
      reset({
        productId: "" as any,
        dealerId: "" as any,
        totalQuantity: 0,
        reservedQuantity: 0,
        availableQuantity: 0,
        inTransitQuantity: 0,
        location: "",
      });
    }, 100);
  };

  const handleEdit = (item: Inventory) => {
    reset({
      productId: item.productId,
      dealerId: item.dealerId || null,
      totalQuantity: item.totalQuantity,
      reservedQuantity: item.reservedQuantity,
      availableQuantity: item.availableQuantity,
      inTransitQuantity: item.inTransitQuantity,
      location: "",
    });
    setSelectedInventory(item);
    setViewMode("edit");
  };

  const handleView = (item: Inventory) => {
    setSelectedInventory(item);
    setViewMode("detail");
  };

  const handleDelete = async (item: Inventory) => {
    if (!confirm(`Bạn có chắc muốn xóa bản ghi tồn kho này?`)) {
      return;
    }
    try {
      await apiClient.delete(`/inventory/${item.inventoryId}`);
      toast.success("Xóa thành công!");
      fetchInventory();
    } catch (error: any) {
      console.error("Error deleting inventory:", error);
      const errorMessage = error.response?.data?.message || "Không thể xóa bản ghi tồn kho";
      toast.error(errorMessage);
    }
  };

  const onSubmit = async (data: InventoryForm) => {
    try {
      // Ensure all values are properly converted
      const payload = {
        productId: data.productId ? Number(data.productId) : null,
        dealerId: data.dealerId ? Number(data.dealerId) : null,
        totalQuantity: Number(data.totalQuantity) || 0,
        reservedQuantity: Number(data.reservedQuantity) || 0,
        availableQuantity: Number(data.availableQuantity) || 0,
        inTransitQuantity: Number(data.inTransitQuantity) || 0,
        location: data.location || "",
      };

      console.log("Payload being sent:", payload);

      if (viewMode === "create") {
        await apiClient.post("/inventory", payload);
        toast.success("Tạo bản ghi tồn kho thành công!");
      } else if (viewMode === "edit" && selectedInventory) {
        await apiClient.put(`/inventory/${selectedInventory.inventoryId}`, payload);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchInventory();
    } catch (error: any) {
      console.error("Error saving inventory:", error);
      const errorMessage = error.response?.data?.message || error.response?.data?.error || "Không thể lưu tồn kho";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Kho hàng</h1>
          <p className="text-muted-foreground">Quản lý tồn kho sản phẩm</p>
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
          <CardTitle>Tìm kiếm</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm..."
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
              <CardTitle>Danh sách tồn kho ({inventory.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {inventory.map((item) => (
                  <div
                    key={item.inventoryId}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
                          {item.isLowStock ? (
                            <AlertTriangle className="h-6 w-6 text-yellow-600" />
                          ) : (
                            <Package className="h-6 w-6 text-primary" />
                          )}
                        </div>
                        <div>
                          <h3 className="font-semibold">{item.productName}</h3>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                            {item.dealerName && (
                              <>
                                <span>Đại lý: {item.dealerName}</span>
                                <span>•</span>
                              </>
                            )}
                            <span>Tổng: {item.totalQuantity}</span>
                            <span>•</span>
                            <span>Có sẵn: {item.availableQuantity}</span>
                            <span>•</span>
                            <span>Đã giữ: {item.reservedQuantity}</span>
                            {item.isLowStock && (
                              <>
                                <span>•</span>
                                <span className="text-yellow-600 font-medium">Tồn kho thấp</span>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-sm text-muted-foreground">Tỷ lệ tồn kho</p>
                        <p className="font-bold text-lg">
                          {item.stockPercentage?.toFixed(1) || 0}%
                        </p>
                      </div>
                      <div className="flex gap-2">
                        <Button variant="outline" size="sm" onClick={() => handleView(item)}>
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleEdit(item)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDelete(item)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
                {inventory.length === 0 && (
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
        title={viewMode === "create" ? "Thêm bản ghi tồn kho" : "Sửa tồn kho"}
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
                console.log("Form validation:", isValid);
                console.log("Form values:", getValues());
                console.log("Form errors:", errors);
                
                if (isValid) {
                  onSubmit(getValues());
                } else {
                  toast.error("Vui lòng điền đầy đủ thông tin bắt buộc");
                }
              }}
              disabled={isSubmitting || loadingDropdowns}
            >
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {/* Debug info */}
          <div className="text-xs text-muted-foreground">
            Loading: {loadingDropdowns ? "Yes" : "No"} | Products: {products.length} | Dealers: {dealers.length}
          </div>
          
          <div>
            <label className="text-sm font-medium">Sản phẩm *</label>
            {loadingDropdowns ? (
              <div className="mt-1 text-sm text-muted-foreground">Đang tải...</div>
            ) : products.length > 0 ? (
              <>
                <select
                  {...register("productId")}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1 focus:outline-none focus:ring-2 focus:ring-ring"
                  disabled={loadingDropdowns}
                >
                  <option value="">-- Chọn sản phẩm --</option>
                  {products.filter(p => p.id).map((product) => (
                    <option key={product.id} value={String(product.id)}>
                      {product.productName}
                    </option>
                  ))}
                </select>
                <p className="text-xs text-muted-foreground mt-1">Có {products.length} sản phẩm</p>
              </>
            ) : (
              <>
                <Input
                  type="number"
                  {...register("productId")}
                  className="mt-1"
                  placeholder="Nhập Product ID"
                />
                <p className="text-xs text-destructive mt-1">Không tải được danh sách sản phẩm</p>
              </>
            )}
            {errors.productId && (
              <p className="text-sm text-destructive mt-1">{errors.productId.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Đại lý (để trống = kho hãng)</label>
            {loadingDropdowns ? (
              <div className="mt-1 text-sm text-muted-foreground">Đang tải...</div>
            ) : dealers.length > 0 ? (
              <>
                <select
                  {...register("dealerId")}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1 focus:outline-none focus:ring-2 focus:ring-ring"
                  disabled={loadingDropdowns}
                >
                  <option value="">-- Kho hãng (để trống) --</option>
                  {dealers.filter(d => d.id).map((dealer) => (
                    <option key={dealer.id} value={String(dealer.id)}>
                      {dealer.dealerName}
                    </option>
                  ))}
                </select>
                <p className="text-xs text-muted-foreground mt-1">Có {dealers.length} đại lý</p>
              </>
            ) : (
              <>
                <Input
                  type="number"
                  {...register("dealerId")}
                  className="mt-1"
                  placeholder="Để trống nếu là kho hãng"
                />
                <p className="text-xs text-destructive mt-1">Không tải được danh sách đại lý</p>
              </>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Tổng số lượng *</label>
              <Input
                type="number"
                {...register("totalQuantity")}
                className="mt-1"
              />
            </div>
            <div>
              <label className="text-sm font-medium">Có sẵn *</label>
              <Input
                type="number"
                {...register("availableQuantity")}
                className="mt-1"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Đã giữ *</label>
              <Input
                type="number"
                {...register("reservedQuantity")}
                className="mt-1"
              />
            </div>
            <div>
              <label className="text-sm font-medium">Đang vận chuyển *</label>
              <Input
                type="number"
                {...register("inTransitQuantity")}
                className="mt-1"
              />
            </div>
          </div>

          <div>
            <label className="text-sm font-medium">Vị trí kho</label>
            <Input {...register("location")} className="mt-1" placeholder="VD: Khu A, Kệ B1" />
          </div>
        </div>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết tồn kho"
        open={viewMode === "detail" && selectedInventory !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedInventory(null);
        }}
        footer={
          <div className="flex gap-2">
            <Button onClick={() => selectedInventory && handleEdit(selectedInventory)}>
              <Edit className="mr-2 h-4 w-4" />
              Sửa
            </Button>
          </div>
        }
      >
        {selectedInventory && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Sản phẩm</label>
              <p className="text-lg font-semibold">{selectedInventory.productName}</p>
            </div>
            {selectedInventory.dealerName && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Đại lý</label>
                <p>{selectedInventory.dealerName}</p>
              </div>
            )}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Tổng số lượng</label>
                <p className="text-2xl font-bold">{selectedInventory.totalQuantity}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Có sẵn</label>
                <p className="text-2xl font-bold text-green-600">
                  {selectedInventory.availableQuantity}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Đã giữ</label>
                <p className="text-2xl font-bold text-yellow-600">
                  {selectedInventory.reservedQuantity}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Đang vận chuyển</label>
                <p className="text-2xl font-bold text-blue-600">
                  {selectedInventory.inTransitQuantity}
                </p>
              </div>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tỷ lệ tồn kho</label>
              <p className="text-2xl font-bold">
                {selectedInventory.stockPercentage?.toFixed(1) || 0}%
              </p>
            </div>
            {selectedInventory.isLowStock && (
              <div className="p-3 rounded-lg bg-yellow-50 border border-yellow-200">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="h-5 w-5 text-yellow-600" />
                  <p className="text-sm font-medium text-yellow-800">Cảnh báo: Tồn kho thấp</p>
                </div>
              </div>
            )}
          </div>
        )}
      </EntityModal>
    </div>
  );
}
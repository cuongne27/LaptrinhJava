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
import type { PaginatedResponse, Product, Dealer } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Check, X, Truck } from "lucide-react";

interface SellInRequest {
  id: number;
  requestNumber: string;
  dealerId: number;
  dealerName: string;
  status: string;
  requestDate: string;
  expectedDeliveryDate?: string;
  actualDeliveryDate?: string;
  deliveryAddress?: string;
  notes?: string;
  approvalNotes?: string;
  totalQuantity: number;
  totalAmount: number;
  createdAt: string;
  items: SellInRequestItem[];
}

interface SellInRequestItem {
  id: number;
  productId: number;
  productName: string;
  productVersion?: string;
  productImageUrl?: string;
  requestedQuantity: number;
  approvedQuantity: number;
  deliveredQuantity?: number;
  color: string;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
}

const sellInRequestSchema = z.object({
  dealerId: z.preprocess(
    (val) => (val === "" || val === "0" || val === 0 ? undefined : Number(val)),
    z.number().min(1, "Vui lòng chọn đại lý")
  ),
  productId: z.preprocess(
    (val) => (val === "" || val === "0" || val === 0 ? undefined : Number(val)),
    z.number().min(1, "Vui lòng chọn sản phẩm")
  ),
  quantity: z.preprocess(
    (val) => (val === "" ? 1 : Number(val)),
    z.number().min(1, "Số lượng phải lớn hơn 0")
  ),
  color: z.string().min(1, "Vui lòng nhập màu"),
  notes: z.string().optional(),
  deliveryAddress: z.string().optional(),
});

type SellInRequestForm = z.infer<typeof sellInRequestSchema>;

export default function SellInRequestsPage() {
  const [requests, setRequests] = useState<SellInRequest[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingDropdowns, setLoadingDropdowns] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedRequest, setSelectedRequest] = useState<SellInRequest | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<SellInRequestForm>({
    resolver: zodResolver(sellInRequestSchema),
  });

  useEffect(() => {
    fetchRequests();
  }, [page]);

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
        const productsData = response.data;
        
        if (productsData.content && Array.isArray(productsData.content)) {
          setProducts(productsData.content);
        } else if (Array.isArray(productsData)) {
          setProducts(productsData);
        } else {
          setProducts([]);
        }
      } catch (error: any) {
        console.error("Error fetching products:", error);
        setProducts([]);
      }

      // Fetch dealers
      try {
        const dealersRes = await apiClient.get("/dealers/filter?page=0&size=100");
        const dealersData = dealersRes.data;
        
        if (dealersData.content && Array.isArray(dealersData.content)) {
          setDealers(dealersData.content);
        } else if (Array.isArray(dealersData)) {
          setDealers(dealersData);
        } else {
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

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      const response = await apiClient.get<PaginatedResponse<SellInRequest>>(
        `/sell-in-requests?${params.toString()}`
      );
      setRequests(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching sell-in requests:", error);
      toast.error("Không thể tải danh sách yêu cầu đặt xe");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedRequest(null);
    setViewMode("create");
    // Reset form after modal opens
    setTimeout(() => {
      reset({
        dealerId: "" as any,
        productId: "" as any,
        quantity: 1,
        color: "",
        notes: "",
        deliveryAddress: "",
      });
    }, 100);
  };

  const handleEdit = (request: SellInRequest) => {
    // Get first item for editing (since we only support single item for now)
    const firstItem = request.items && request.items.length > 0 ? request.items[0] : null;
    
    reset({
      dealerId: request.dealerId,
      productId: firstItem?.productId || ("" as any),
      quantity: firstItem?.requestedQuantity || 1,
      color: firstItem?.color || "",
      notes: request.notes || "",
      deliveryAddress: request.deliveryAddress || "",
    });
    setSelectedRequest(request);
    setViewMode("edit");
  };

  const handleView = (request: SellInRequest) => {
    setSelectedRequest(request);
    setViewMode("detail");
  };

  const handleDelete = async (request: SellInRequest) => {
    if (!confirm(`Bạn có chắc muốn xóa yêu cầu "${request.requestNumber}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/sell-in-requests/${request.id}`);
      toast.success("Xóa thành công!");
      fetchRequests();
    } catch (error) {
      console.error("Error deleting request:", error);
      toast.error("Không thể xóa yêu cầu");
    }
  };

  const handleApprove = async (request: SellInRequest) => {
    if (!confirm(`Phê duyệt yêu cầu "${request.requestNumber}"?`)) {
      return;
    }
    try {
      await apiClient.post(`/sell-in-requests/${request.id}/approve`);
      toast.success("Phê duyệt thành công!");
      fetchRequests();
    } catch (error) {
      console.error("Error approving request:", error);
      toast.error("Không thể phê duyệt yêu cầu");
    }
  };

  const handleReject = async (request: SellInRequest) => {
    if (!confirm(`Từ chối yêu cầu "${request.requestNumber}"?`)) {
      return;
    }
    try {
      await apiClient.post(`/sell-in-requests/${request.id}/reject`);
      toast.success("Từ chối thành công!");
      fetchRequests();
    } catch (error) {
      console.error("Error rejecting request:", error);
      toast.error("Không thể từ chối yêu cầu");
    }
  };

  const handleMarkDelivered = async (request: SellInRequest) => {
    if (!confirm(`Đánh dấu đã giao xe cho yêu cầu "${request.requestNumber}"?`)) {
      return;
    }
    try {
      // Fix: Use correct endpoint - first mark as in-transit if status is APPROVED
      if (request.status === "APPROVED") {
        await apiClient.post(`/sell-in-requests/${request.id}/mark-in-transit`);
        toast.success("Đã chuyển sang trạng thái vận chuyển!");
      } else if (request.status === "IN_TRANSIT") {
        await apiClient.post(`/sell-in-requests/${request.id}/mark-delivered`);
        toast.success("Đánh dấu đã giao thành công!");
      }
      fetchRequests();
    } catch (error) {
      console.error("Error marking delivered:", error);
      toast.error("Không thể cập nhật trạng thái");
    }
  };

  const onSubmit = async (data: SellInRequestForm) => {
    try {
      // Build payload matching backend DTO structure
      const payload = {
        dealerId: Number(data.dealerId),
        requestDate: new Date().toISOString().split('T')[0], // Current date in YYYY-MM-DD format
        expectedDeliveryDate: null,
        items: [
          {
            productId: Number(data.productId),
            quantity: Number(data.quantity),
            color: data.color,
            notes: data.notes || null,
          }
        ],
        notes: data.notes || null,
        deliveryAddress: data.deliveryAddress || null,
      };

      console.log("Payload being sent:", payload);

      if (viewMode === "create") {
        await apiClient.post("/sell-in-requests", payload);
        toast.success("Tạo yêu cầu thành công!");
      } else if (viewMode === "edit" && selectedRequest) {
        await apiClient.put(`/sell-in-requests/${selectedRequest.id}`, payload);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchRequests();
    } catch (error: any) {
      console.error("Error saving request:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu yêu cầu";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Đặt hàng từ Hãng</h1>
          <p className="text-muted-foreground">Quản lý yêu cầu đặt xe từ Hãng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchRequests} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Làm mới
          </Button>
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm mới
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Đang tải...</div>
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {requests.map((request) => {
              // Get first item to display (since we support single item per request for now)
              const firstItem = request.items && request.items.length > 0 ? request.items[0] : null;
              
              return (
                <Card key={request.id} className="overflow-hidden">
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-lg">{request.requestNumber}</CardTitle>
                      <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(request.status)}`}>
                        {request.status}
                      </span>
                    </div>
                    <CardDescription>
                      {formatDate(request.requestDate)}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 mb-4">
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Đại lý</span>
                        <span className="font-medium">{request.dealerName}</span>
                      </div>
                      {firstItem && (
                        <>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-muted-foreground">Sản phẩm</span>
                            <span className="font-medium">{firstItem.productName}</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-muted-foreground">Số lượng</span>
                            <span className="font-medium">{firstItem.requestedQuantity}</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-muted-foreground">Màu</span>
                            <span className="font-medium">{firstItem.color}</span>
                          </div>
                        </>
                      )}
                      {request.items && request.items.length > 1 && (
                        <div className="text-xs text-muted-foreground">
                          +{request.items.length - 1} sản phẩm khác
                        </div>
                      )}
                    </div>
                    <div className="flex gap-2 flex-wrap">
                      <Button variant="outline" size="sm" onClick={() => handleView(request)}>
                        <Eye className="h-4 w-4" />
                      </Button>
                      {request.status === "PENDING" && (
                        <>
                          <Button variant="outline" size="sm" onClick={() => handleApprove(request)}>
                            <Check className="h-4 w-4" />
                          </Button>
                          <Button variant="outline" size="sm" onClick={() => handleReject(request)}>
                            <X className="h-4 w-4" />
                          </Button>
                          <Button variant="outline" size="sm" onClick={() => handleEdit(request)}>
                            <Edit className="h-4 w-4" />
                          </Button>
                        </>
                      )}
                      {request.status === "APPROVED" && (
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleMarkDelivered(request)}
                          title="Chuyển sang vận chuyển"
                        >
                          <Truck className="h-4 w-4" />
                        </Button>
                      )}
                      {request.status === "IN_TRANSIT" && (
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleMarkDelivered(request)}
                          title="Đánh dấu đã giao"
                        >
                          <Check className="h-4 w-4 text-green-600" />
                        </Button>
                      )}
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDelete(request)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
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
        title={viewMode === "create" ? "Thêm yêu cầu mới" : "Sửa yêu cầu"}
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
          <div>
            <label className="text-sm font-medium">Đại lý *</label>
            {loadingDropdowns ? (
              <div className="mt-1 text-sm text-muted-foreground">Đang tải...</div>
            ) : dealers.length > 0 ? (
              <>
                <select
                  {...register("dealerId")}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1 focus:outline-none focus:ring-2 focus:ring-ring"
                  disabled={loadingDropdowns}
                >
                  <option value="">-- Chọn đại lý --</option>
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
                  placeholder="Nhập Dealer ID"
                />
                <p className="text-xs text-destructive mt-1">Không tải được danh sách đại lý</p>
              </>
            )}
            {errors.dealerId && (
              <p className="text-sm text-destructive mt-1">{errors.dealerId.message}</p>
            )}
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
            <label className="text-sm font-medium">Số lượng *</label>
            <Input
              type="number"
              {...register("quantity")}
              className="mt-1"
              min="1"
            />
            {errors.quantity && (
              <p className="text-sm text-destructive mt-1">{errors.quantity.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Màu *</label>
            <Input {...register("color")} className="mt-1" placeholder="VD: Đỏ, Xanh, Trắng" />
            {errors.color && (
              <p className="text-sm text-destructive mt-1">{errors.color.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Địa chỉ giao hàng</label>
            <Input {...register("deliveryAddress")} className="mt-1" placeholder="Để trống sẽ dùng địa chỉ đại lý" />
          </div>

          <div>
            <label className="text-sm font-medium">Ghi chú</label>
            <textarea
              {...register("notes")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              placeholder="Thêm ghi chú..."
            />
          </div>
        </div>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết yêu cầu"
        open={viewMode === "detail" && selectedRequest !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedRequest(null);
        }}
        footer={
          selectedRequest?.status === "PENDING" ? (
            <Button onClick={() => selectedRequest && handleEdit(selectedRequest)}>
              <Edit className="mr-2 h-4 w-4" />
              Sửa
            </Button>
          ) : null
        }
      >
        {selectedRequest && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Số yêu cầu</label>
              <p className="text-lg font-semibold">{selectedRequest.requestNumber}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedRequest.status)}`}>
                  {selectedRequest.status}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Đại lý</label>
              <p>{selectedRequest.dealerName}</p>
            </div>
            
            {/* Display all items */}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Danh sách sản phẩm</label>
              <div className="mt-2 space-y-3">
                {selectedRequest.items && selectedRequest.items.map((item, index) => (
                  <div key={item.id || index} className="p-3 border rounded-lg bg-accent/50">
                    <div className="grid grid-cols-2 gap-2 text-sm">
                      <div>
                        <span className="text-muted-foreground">Sản phẩm:</span>
                        <p className="font-medium">{item.productName}</p>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Số lượng:</span>
                        <p className="font-medium">{item.requestedQuantity}</p>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Màu:</span>
                        <p className="font-medium">{item.color}</p>
                      </div>
                      {item.approvedQuantity > 0 && (
                        <div>
                          <span className="text-muted-foreground">Đã duyệt:</span>
                          <p className="font-medium text-green-600">{item.approvedQuantity}</p>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Tổng số lượng</label>
                <p className="text-2xl font-bold">{selectedRequest.totalQuantity}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Tổng giá trị</label>
                <p className="text-2xl font-bold text-primary">
                  {selectedRequest.totalAmount?.toLocaleString('vi-VN')} ₫
                </p>
              </div>
            </div>

            <div>
              <label className="text-sm font-medium text-muted-foreground">Ngày yêu cầu</label>
              <p>{formatDate(selectedRequest.requestDate)}</p>
            </div>
            {selectedRequest.expectedDeliveryDate && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ngày giao dự kiến</label>
                <p>{formatDate(selectedRequest.expectedDeliveryDate)}</p>
              </div>
            )}
            {selectedRequest.actualDeliveryDate && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ngày giao thực tế</label>
                <p>{formatDate(selectedRequest.actualDeliveryDate)}</p>
              </div>
            )}
            {selectedRequest.deliveryAddress && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Địa chỉ giao hàng</label>
                <p>{selectedRequest.deliveryAddress}</p>
              </div>
            )}
            {selectedRequest.notes && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ghi chú</label>
                <p>{selectedRequest.notes}</p>
              </div>
            )}
            {selectedRequest.approvalNotes && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Ghi chú phê duyệt</label>
                <p>{selectedRequest.approvalNotes}</p>
              </div>
            )}
          </div>
        )}
      </EntityModal>
    </div>
  );
}
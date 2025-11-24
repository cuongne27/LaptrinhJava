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
import type { PaginatedResponse } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Check, X, Truck } from "lucide-react";

interface SellInRequest {
  id: number;
  requestNumber: string;
  dealerId: number;
  dealerName: string;
  productId: number;
  productName: string;
  quantity: number;
  color: string;
  status: string;
  requestDate: string;
  expectedDeliveryDate?: string;
  createdAt: string;
}

const sellInRequestSchema = z.object({
  dealerId: z.number().min(1, "Vui lòng chọn đại lý"),
  requestDate: z.string().min(1, "Vui lòng chọn ngày yêu cầu"),
  expectedDeliveryDate: z.string().optional(),
  deliveryAddress: z.string().optional(),
  notes: z.string().optional(),
  // Items as separate fields (will be transformed)
  productId: z.number().min(1, "Vui lòng chọn sản phẩm"),
  quantity: z.number().min(1, "Số lượng phải lớn hơn 0"),
  color: z.string().min(1, "Vui lòng chọn màu"),
  itemNotes: z.string().optional(),
});

type SellInRequestForm = z.infer<typeof sellInRequestSchema>;

export default function SellInRequestsPage() {
  const [requests, setRequests] = useState<SellInRequest[]>([]);
  const [loading, setLoading] = useState(true);
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
    defaultValues: {
      dealerId: 0,
      requestDate: new Date().toISOString().split('T')[0],
      productId: 0,
      quantity: 1,
      color: "",
    }
  });

  useEffect(() => {
    fetchRequests();
  }, [page]);

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
    reset({
      dealerId: 0,
      requestDate: new Date().toISOString().split('T')[0],
      productId: 0,
      quantity: 1,
      color: "",
      expectedDeliveryDate: "",
      deliveryAddress: "",
      notes: "",
      itemNotes: "",
    });
    setSelectedRequest(null);
    setViewMode("create");
  };

  const handleEdit = (request: SellInRequest) => {
    reset({
      dealerId: request.dealerId,
      requestDate: request.requestDate,
      productId: request.productId,
      quantity: request.quantity,
      color: request.color,
      expectedDeliveryDate: request.expectedDeliveryDate || "",
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
      await apiClient.post(`/sell-in-requests/${request.id}/mark-delivered`);
      toast.success("Đánh dấu đã giao thành công!");
      fetchRequests();
    } catch (error) {
      console.error("Error marking delivered:", error);
      toast.error("Không thể đánh dấu đã giao");
    }
  };

  const onSubmit = async (data: SellInRequestForm) => {
    console.log("Form data:", data);
    
    try {
      // ✅ Transform to backend expected format
      const payload = {
        dealerId: data.dealerId,
        requestDate: data.requestDate,
        expectedDeliveryDate: data.expectedDeliveryDate || undefined,
        deliveryAddress: data.deliveryAddress || undefined,
        notes: data.notes || undefined,
        items: [
          {
            productId: data.productId,
            quantity: data.quantity,
            color: data.color,
            notes: data.itemNotes || undefined,
          }
        ]
      };

      // Remove undefined values
      Object.keys(payload).forEach(key => {
        if (payload[key as keyof typeof payload] === undefined) {
          delete payload[key as keyof typeof payload];
        }
      });

      console.log("Payload being sent:", JSON.stringify(payload, null, 2));

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
      console.error("Error response:", error.response?.data);
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
            {requests.map((request) => (
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
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Sản phẩm</span>
                      <span className="font-medium">{request.productName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Số lượng</span>
                      <span className="font-medium">{request.quantity}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Màu</span>
                      <span className="font-medium">{request.color}</span>
                    </div>
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
                      <Button variant="outline" size="sm" onClick={() => handleMarkDelivered(request)}>
                        <Truck className="h-4 w-4" />
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
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium">Đại lý ID *</label>
            <Input
              type="number"
              {...register("dealerId", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.dealerId && (
              <p className="text-sm text-destructive mt-1">{errors.dealerId.message}</p>
            )}
          </div>
          
          <div>
            <label className="text-sm font-medium">Ngày yêu cầu *</label>
            <Input
              type="date"
              {...register("requestDate")}
              className="mt-1"
            />
            {errors.requestDate && (
              <p className="text-sm text-destructive mt-1">{errors.requestDate.message}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium">Ngày giao dự kiến</label>
            <Input 
              type="date" 
              {...register("expectedDeliveryDate")} 
              className="mt-1" 
            />
          </div>

          <div className="border-t pt-4">
            <h3 className="font-semibold mb-3">Thông tin sản phẩm</h3>
            
            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium">Sản phẩm ID *</label>
                <Input
                  type="number"
                  {...register("productId", { valueAsNumber: true })}
                  className="mt-1"
                />
                {errors.productId && (
                  <p className="text-sm text-destructive mt-1">{errors.productId.message}</p>
                )}
              </div>
              
              <div>
                <label className="text-sm font-medium">Số lượng *</label>
                <Input
                  type="number"
                  {...register("quantity", { valueAsNumber: true })}
                  className="mt-1"
                />
                {errors.quantity && (
                  <p className="text-sm text-destructive mt-1">{errors.quantity.message}</p>
                )}
              </div>
              
              <div>
                <label className="text-sm font-medium">Màu *</label>
                <Input {...register("color")} className="mt-1" />
                {errors.color && (
                  <p className="text-sm text-destructive mt-1">{errors.color.message}</p>
                )}
              </div>

              <div>
                <label className="text-sm font-medium">Ghi chú sản phẩm</label>
                <Input {...register("itemNotes")} className="mt-1" placeholder="Ghi chú cho sản phẩm này" />
              </div>
            </div>
          </div>

          <div className="border-t pt-4">
            <h3 className="font-semibold mb-3">Thông tin bổ sung</h3>
            
            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium">Địa chỉ giao hàng</label>
                <Input 
                  {...register("deliveryAddress")} 
                  className="mt-1" 
                  placeholder="Để trống sẽ dùng địa chỉ của đại lý"
                />
              </div>

              <div>
                <label className="text-sm font-medium">Ghi chú chung</label>
                <textarea
                  {...register("notes")}
                  className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
                  placeholder="Ghi chú chung cho yêu cầu"
                />
              </div>
            </div>
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
            <div>
              <label className="text-sm font-medium text-muted-foreground">Sản phẩm</label>
              <p>{selectedRequest.productName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Số lượng</label>
              <p>{selectedRequest.quantity}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Màu</label>
              <p>{selectedRequest.color}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
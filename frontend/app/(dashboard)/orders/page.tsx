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
import type { SalesOrder, PaginatedResponse, Customer, Product } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Package, X } from "lucide-react";

const orderSchema = z.object({
  orderDate: z.string().min(1, "Ngày đặt hàng không được để trống"),
  basePrice: z.number().min(0.01, "Giá cơ bản phải lớn hơn 0"),
  vat: z.number().min(0),
  registrationFee: z.number().min(0).optional(),
  discountAmount: z.number().min(0).optional(),
  totalPrice: z.number().min(0.01, "Tổng giá phải lớn hơn 0"),
  status: z.string().optional(),
  vehicleId: z.string().min(1, "Vehicle ID không được để trống"),
  customerId: z.number().min(1, "Vui lòng chọn khách hàng"),
  salesPersonId: z.number().min(1, "Vui lòng chọn nhân viên bán hàng"),
});

type OrderForm = z.infer<typeof orderSchema>;

export default function OrdersPage() {
  const [orders, setOrders] = useState<SalesOrder[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedOrder, setSelectedOrder] = useState<SalesOrder | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<OrderForm>({
    resolver: zodResolver(orderSchema),
  });

  useEffect(() => {
    fetchOrders();
    fetchCustomers();
  }, [page, search]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      const response = await apiClient.get<PaginatedResponse<SalesOrder>>(
        `/sales-orders?${params.toString()}`
      );
      setOrders(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      console.error("Error fetching orders:", error);
      toast.error("Không thể tải danh sách đơn hàng");
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomers = async () => {
    try {
      const response = await apiClient.get<PaginatedResponse<Customer>>("/customers?size=100");
      setCustomers(response.data.content || []);
    } catch (error) {
      console.error("Error fetching customers:", error);
      setCustomers([]);
    }
  };

  const handleCreate = () => {
    reset({
      orderDate: new Date().toISOString().split("T")[0],
      basePrice: 0,
      vat: 0,
      registrationFee: 0,
      discountAmount: 0,
      totalPrice: 0,
      status: "PENDING",
      vehicleId: "",
      customerId: customers[0]?.id || 0,
      salesPersonId: 1,
    });
    setSelectedOrder(null);
    setViewMode("create");
  };

  const handleEdit = (order: SalesOrder) => {
    reset({
      orderDate: order.orderDate,
      basePrice: Number(order.totalPrice),
      vat: 0,
      registrationFee: 0,
      discountAmount: 0,
      totalPrice: Number(order.totalPrice),
      status: order.status,
      vehicleId: order.vehicleId || "",
      customerId: order.customerId,
      salesPersonId: order.salesPersonId,
    });
    setSelectedOrder(order);
    setViewMode("edit");
  };

  const handleView = (order: SalesOrder) => {
    setSelectedOrder(order);
    setViewMode("detail");
  };

  const handleDelete = async (order: SalesOrder) => {
    if (!confirm(`Bạn có chắc muốn xóa đơn hàng #${order.id}?`)) {
      return;
    }
    try {
      await apiClient.delete(`/sales-orders/${order.id}`);
      toast.success("Xóa thành công!");
      fetchOrders();
    } catch (error: any) {
      console.error("Error deleting order:", error);
      const errorMessage = error.response?.data?.message || "Không thể xóa đơn hàng";
      toast.error(errorMessage);
    }
  };

  const handleCancel = async (order: SalesOrder) => {
    if (!confirm(`Bạn có chắc muốn hủy đơn hàng #${order.id}?`)) {
      return;
    }
    try {
      // ✅ Dùng PATCH như backend đã define (cần fix CORS trong backend)
      await apiClient.patch(`/sales-orders/${order.id}/cancel`);
      toast.success("Hủy đơn hàng thành công!");
      fetchOrders();
    } catch (error: any) {
      console.error("Error canceling order:", error);
      const errorMessage = 
        error.response?.data?.message || 
        "Không thể hủy đơn hàng. Vui lòng kiểm tra CORS configuration trong backend.";
      toast.error(errorMessage);
    }
  };

  const handleUpdateStatus = async (orderId: number, newStatus: string) => {
    try {
      // ✅ Sử dụng PATCH với query parameter như backend đã define
      await apiClient.patch(`/sales-orders/${orderId}/status?status=${newStatus}`);
      toast.success("Cập nhật trạng thái thành công!");
      fetchOrders();
    } catch (error: any) {
      console.error("Error updating status:", error);
      const errorMessage = error.response?.data?.message || "Không thể cập nhật trạng thái";
      toast.error(errorMessage);
    }
  };

  const onSubmit = async (data: OrderForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/sales-orders", data);
        toast.success("Tạo đơn hàng thành công!");
      } else if (viewMode === "edit" && selectedOrder) {
        await apiClient.put(`/sales-orders/${selectedOrder.id}`, data);
        // console.log("data", data);
        // console.log("selectedOrder", selectedOrder);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchOrders();
    } catch (error: any) {
      console.error("Error saving order:", error);
      const errorMessage = error.response?.data?.message || "Không thể lưu đơn hàng";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Đơn hàng</h1>
          <p className="text-muted-foreground">Quản lý đơn hàng bán xe điện</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchOrders} disabled={loading}>
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
              placeholder="Tìm kiếm đơn hàng..."
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
              <CardTitle>Danh sách đơn hàng ({orders.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {orders.map((order) => (
                  <div
                    key={order.id}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
                          <Package className="h-6 w-6 text-primary" />
                        </div>
                        <div>
                          <div className="flex items-center gap-3">
                            <h3 className="font-semibold">Đơn hàng #{order.id}</h3>
                            <span
                              className={`px-2 py-1 text-xs rounded-full ${getStatusColor(
                                order.status
                              )}`}
                            >
                              {order.status}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                            <span>Khách hàng: {order.customerName}</span>
                            <span>•</span>
                            <span>Ngày: {formatDate(order.orderDate)}</span>
                            {order.vehicleModel && (
                              <>
                                <span>•</span>
                                <span>Xe: {order.vehicleModel}</span>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-sm text-muted-foreground">Tổng tiền</p>
                        <p className="font-bold text-lg">{formatCurrency(order.totalPrice)}</p>
                        {order.isPaid !== undefined && (
                          <p className="text-xs text-muted-foreground">
                            {order.isPaid ? "Đã thanh toán" : "Chưa thanh toán"}
                          </p>
                        )}
                      </div>
                      <div className="flex gap-2">
                        <Button variant="outline" size="sm" onClick={() => handleView(order)}>
                          <Eye className="mr-2 h-4 w-4" />
                          Xem
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleEdit(order)}>
                          <Edit className="mr-2 h-4 w-4" />
                          Sửa
                        </Button>
                        {order.status !== "CANCELLED" && order.status !== "COMPLETED" && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleCancel(order)}
                          >
                            <X className="mr-2 h-4 w-4" />
                            Hủy
                          </Button>
                        )}
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDelete(order)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          Xóa
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
                {orders.length === 0 && (
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
        title={viewMode === "create" ? "Thêm đơn hàng mới" : "Sửa đơn hàng"}
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
            <label className="text-sm font-medium">Ngày đặt hàng *</label>
            <Input type="date" {...register("orderDate")} className="mt-1" />
            {errors.orderDate && (
              <p className="text-sm text-destructive mt-1">{errors.orderDate.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Khách hàng *</label>
            <select
              {...register("customerId", { valueAsNumber: true })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn khách hàng</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.fullName} - {customer.phoneNumber}
                </option>
              ))}
            </select>
            {errors.customerId && (
              <p className="text-sm text-destructive mt-1">{errors.customerId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Vehicle ID (VIN) *</label>
            <Input {...register("vehicleId")} className="mt-1" placeholder="Nhập VIN" />
            {errors.vehicleId && (
              <p className="text-sm text-destructive mt-1">{errors.vehicleId.message}</p>
            )}
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Giá cơ bản (VND) *</label>
              <Input
                type="number"
                step="0.01"
                {...register("basePrice", { valueAsNumber: true })}
                className="mt-1"
              />
              {errors.basePrice && (
                <p className="text-sm text-destructive mt-1">{errors.basePrice.message}</p>
              )}
            </div>
            <div>
              <label className="text-sm font-medium">VAT (VND) *</label>
              <Input
                type="number"
                step="0.01"
                {...register("vat", { valueAsNumber: true })}
                className="mt-1"
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Phí đăng ký (VND)</label>
              <Input
                type="number"
                step="0.01"
                {...register("registrationFee", { valueAsNumber: true })}
                className="mt-1"
              />
            </div>
            <div>
              <label className="text-sm font-medium">Giảm giá (VND)</label>
              <Input
                type="number"
                step="0.01"
                {...register("discountAmount", { valueAsNumber: true })}
                className="mt-1"
              />
            </div>
          </div>
          <div>
            <label className="text-sm font-medium">Tổng giá (VND) *</label>
            <Input
              type="number"
              step="0.01"
              {...register("totalPrice", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.totalPrice && (
              <p className="text-sm text-destructive mt-1">{errors.totalPrice.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Trạng thái</label>
            <select
              {...register("status")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="PENDING">PENDING</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="PAID">PAID</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
          <div>
            <label className="text-sm font-medium">Nhân viên bán hàng ID *</label>
            <Input
              type="number"
              {...register("salesPersonId", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.salesPersonId && (
              <p className="text-sm text-destructive mt-1">{errors.salesPersonId.message}</p>
            )}
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết đơn hàng"
        open={viewMode === "detail" && selectedOrder !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedOrder(null);
        }}
        footer={
          <div className="flex gap-2">
            {selectedOrder && selectedOrder.status !== "CANCELLED" && (
              <>
                <Button
                  variant="outline"
                  onClick={() =>
                    selectedOrder &&
                    handleUpdateStatus(selectedOrder.id, "CONFIRMED")
                  }
                >
                  Xác nhận
                </Button>
                <Button
                  variant="outline"
                  onClick={() => selectedOrder && handleCancel(selectedOrder)}
                >
                  Hủy đơn
                </Button>
              </>
            )}
            <Button onClick={() => selectedOrder && handleEdit(selectedOrder)}>
              <Edit className="mr-2 h-4 w-4" />
              Sửa
            </Button>
          </div>
        }
      >
        {selectedOrder && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Mã đơn hàng</label>
              <p className="text-lg font-semibold">#{selectedOrder.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <span
                className={`inline-block px-2 py-1 text-xs rounded-full ${getStatusColor(
                  selectedOrder.status
                )}`}
              >
                {selectedOrder.status}
              </span>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Ngày đặt hàng</label>
              <p>{formatDate(selectedOrder.orderDate)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Khách hàng</label>
              <p>{selectedOrder.customerName}</p>
            </div>
            {selectedOrder.vehicleModel && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Xe</label>
                <p>
                  {selectedOrder.vehicleBrand} {selectedOrder.vehicleModel}
                </p>
              </div>
            )}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tổng tiền</label>
              <p className="text-2xl font-bold text-primary">
                {formatCurrency(selectedOrder.totalPrice)}
              </p>
            </div>
            {selectedOrder.isPaid !== undefined && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Thanh toán</label>
                <p>{selectedOrder.isPaid ? "Đã thanh toán" : "Chưa thanh toán"}</p>
                {selectedOrder.paidAmount !== undefined && (
                  <p className="text-sm">Đã thanh: {formatCurrency(selectedOrder.paidAmount)}</p>
                )}
                {selectedOrder.remainingAmount !== undefined && (
                  <p className="text-sm">
                    Còn lại: {formatCurrency(selectedOrder.remainingAmount)}
                  </p>
                )}
              </div>
            )}
          </div>
        )}
      </EntityModal>
    </div>
  );
}
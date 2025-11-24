"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api/client";
import { EntityModal } from "@/components/entity/EntityModal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import toast from "react-hot-toast";
import { formatDateTime, getStatusColor } from "@/lib/utils";
import type { PaginatedResponse, Customer, Dealer, Product } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, X } from "lucide-react";

interface Appointment {
  id: number;
  appointmentTime: string;
  status: string;
  customerId: number;
  customerName: string;
  customerPhone?: string;
  staffUserId?: number | null;
  staffName?: string;
  productId: number;
  productName: string;
  dealerId: number;
  dealerName: string;
  notes?: string;
  isUpcoming?: boolean;
  isToday?: boolean;
  hoursUntil?: number;
}

interface StaffUserOption {
  userId: number;
  fullName: string;
  email?: string;
  dealerName?: string;
}

const appointmentSchema = z.object({
  customerId: z.number().min(1, "Vui lòng chọn khách hàng"),
  staffUserId: z.number().optional(),
  productId: z.number().min(1, "Vui lòng chọn sản phẩm"),
  dealerId: z.number().min(1, "Vui lòng chọn đại lý"),
  appointmentTime: z.string().min(1, "Vui lòng chọn thời gian hẹn"),
  notes: z.string().optional(),
});

type AppointmentForm = z.infer<typeof appointmentSchema>;

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState<"all" | "today" | "upcoming">("all");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");
  const [customerOptions, setCustomerOptions] = useState<Customer[]>([]);
  const [staffOptions, setStaffOptions] = useState<StaffUserOption[]>([]);
  const [dealerOptions, setDealerOptions] = useState<Dealer[]>([]);
  const [productOptions, setProductOptions] = useState<Product[]>([]);
  const [metadataLoading, setMetadataLoading] = useState(false);

  const getLocalDateTimeValue = (value?: string | null) => {
    if (!value) {
      return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "";
    }
    const tzOffset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - tzOffset).toISOString().slice(0, 16);
  };

  const toIsoWithOffset = (value: string) => {
    const date = new Date(value);
    return date.toISOString();
  };

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<AppointmentForm>({
    resolver: zodResolver(appointmentSchema),
  });

  const fetchAppointments = useCallback(async () => {
    try {
      setLoading(true);
      let url = `/appointments`;
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });

      if (filterType === "today") {
        url = `/appointments/today`;
      } else if (filterType === "upcoming") {
        url = `/appointments/upcoming`;
      }

      url += url.includes("?") ? `&${params.toString()}` : `?${params.toString()}`;

      const response = await apiClient.get<PaginatedResponse<Appointment>>(url);
      
      // ✅ Fix: Đảm bảo appointments luôn là array
      setAppointments(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      console.error("Error fetching appointments:", error);
      toast.error("Không thể tải danh sách lịch hẹn");
      // ✅ Set empty array khi có lỗi
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  }, [filterType, page]);

  useEffect(() => {
    fetchAppointments();
  }, [fetchAppointments]);

  useEffect(() => {
    loadMetadata();
  }, []);

  const loadMetadata = async () => {
    try {
      setMetadataLoading(true);
      const query = new URLSearchParams({
        page: "0",
        size: "100",
      }).toString();

      const [customersRes, staffRes, dealersRes, productsRes] = await Promise.all([
        apiClient.get<PaginatedResponse<Customer>>(`/customers?${query}`),
        apiClient.get<PaginatedResponse<StaffUserOption>>(`/users?roleName=DEALER_STAFF&${query}`),
        apiClient.get<PaginatedResponse<Dealer>>(`/dealers/filter?${query}`),
        apiClient.get<PaginatedResponse<Product>>(`/products?${query}`),
      ]);

      setCustomerOptions(customersRes.data.content || []);
      setStaffOptions(staffRes.data.content || []);
      setDealerOptions(dealersRes.data.content || []);
      setProductOptions(productsRes.data.content || []);
    } catch (error) {
      console.error("Error loading appointment metadata:", error);
      toast.error("Không thể tải dữ liệu hỗ trợ lịch hẹn");
    } finally {
      setMetadataLoading(false);
    }
  };

  const handleCreate = () => {
    reset({
      customerId: 0,
      staffUserId: 0,
      productId: 0,
      dealerId: 0,
      appointmentTime: "",
      notes: "",
    });
    setSelectedAppointment(null);
    setViewMode("create");
  };

  const handleEdit = (appointment: Appointment) => {
    reset({
      customerId: appointment.customerId,
      staffUserId: appointment.staffUserId ?? 0,
      productId: appointment.productId,
      dealerId: appointment.dealerId,
      appointmentTime: getLocalDateTimeValue(appointment.appointmentTime),
      notes: appointment.notes || "",
    });
    setSelectedAppointment(appointment);
    setViewMode("edit");
  };

  const handleView = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setViewMode("detail");
  };

  const handleDelete = async (appointment: Appointment) => {
    if (!confirm(`Bạn có chắc muốn xóa lịch hẹn #${appointment.id}?`)) {
      return;
    }
    try {
      await apiClient.delete(`/appointments/${appointment.id}`);
      toast.success("Xóa thành công!");
      fetchAppointments();
    } catch (error) {
      console.error("Error deleting appointment:", error);
      toast.error("Không thể xóa lịch hẹn");
    }
  };

  const handleCancel = async (appointment: Appointment) => {
    if (!confirm(`Bạn có chắc muốn hủy lịch hẹn #${appointment.id}?`)) {
      return;
    }
    try {
      await apiClient.patch(`/appointments/${appointment.id}/cancel`);
      toast.success("Hủy lịch hẹn thành công!");
      fetchAppointments();
    } catch (error) {
      console.error("Error canceling appointment:", error);
      toast.error("Không thể hủy lịch hẹn");
    }
  };

  const onSubmit = async (data: AppointmentForm) => {
    try {
      const payload = {
        ...data,
        appointmentTime: toIsoWithOffset(data.appointmentTime),
        staffUserId: data.staffUserId && data.staffUserId > 0 ? data.staffUserId : undefined,
        notes: data.notes?.trim() ? data.notes : undefined,
      };

      if (viewMode === "create") {
        await apiClient.post("/appointments", payload);
        toast.success("Tạo lịch hẹn thành công!");
      } else if (viewMode === "edit" && selectedAppointment) {
        await apiClient.put(`/appointments/${selectedAppointment.id}`, payload);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchAppointments();
    } catch (error: any) {
      console.error("Error saving appointment:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu lịch hẹn";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Lịch hẹn</h1>
          <p className="text-muted-foreground">Quản lý lịch hẹn với khách hàng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchAppointments} disabled={loading}>
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
          <CardTitle>Lọc lịch hẹn</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <Button
              variant={filterType === "all" ? "default" : "outline"}
              onClick={() => {
                setFilterType("all");
                setPage(0);
              }}
            >
              Tất cả
            </Button>
            <Button
              variant={filterType === "today" ? "default" : "outline"}
              onClick={() => {
                setFilterType("today");
                setPage(0);
              }}
            >
              Hôm nay
            </Button>
            <Button
              variant={filterType === "upcoming" ? "default" : "outline"}
              onClick={() => {
                setFilterType("upcoming");
                setPage(0);
              }}
            >
              Sắp tới
            </Button>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Đang tải...</div>
        </div>
      ) : appointments.length === 0 ? (
        // ✅ Add: Empty state khi không có data
        <Card>
          <CardContent className="flex flex-col items-center justify-center h-64">
            <p className="text-muted-foreground mb-4">Chưa có lịch hẹn nào</p>
            <Button onClick={handleCreate}>
              <Plus className="mr-2 h-4 w-4" />
              Tạo lịch hẹn đầu tiên
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {appointments.map((appointment) => (
              <Card key={appointment.id} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">#{appointment.id}</CardTitle>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(appointment.status)}`}>
                      {appointment.status}
                    </span>
                  </div>
                  <CardDescription>
                    {formatDateTime(appointment.appointmentTime)}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Khách hàng</span>
                      <span className="font-medium">{appointment.customerName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Nhân viên</span>
                      <span className="font-medium">
                        {appointment.staffName || "Chưa phân công"}
                      </span>
                    </div>
                    {appointment.productName && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Sản phẩm</span>
                        <span className="font-medium">{appointment.productName}</span>
                      </div>
                    )}
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <Button variant="outline" size="sm" onClick={() => handleView(appointment)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    {appointment.status !== "CANCELLED" && appointment.status !== "COMPLETED" && (
                      <>
                        <Button variant="outline" size="sm" onClick={() => handleEdit(appointment)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleCancel(appointment)}>
                          <X className="h-4 w-4" />
                        </Button>
                      </>
                    )}
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(appointment)}
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
        title={viewMode === "create" ? "Thêm lịch hẹn mới" : "Sửa lịch hẹn"}
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
            <label className="text-sm font-medium">Khách hàng *</label>
            <select
              {...register("customerId", { valueAsNumber: true })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              disabled={metadataLoading}
            >
              <option value={0}>Chọn khách hàng</option>
              {customerOptions.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.fullName}
                  {customer.phoneNumber ? ` (${customer.phoneNumber})` : ""}
                </option>
              ))}
            </select>
            {errors.customerId && (
              <p className="text-sm text-destructive mt-1">{errors.customerId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Nhân viên phụ trách</label>
            <select
              {...register("staffUserId", {
                setValueAs: (value) => (value === "" ? undefined : Number(value)),
              })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              disabled={metadataLoading}
            >
              <option value="">Tự động phân công</option>
              {staffOptions.map((staff) => (
                <option key={staff.userId} value={staff.userId}>
                  {staff.fullName}
                  {staff.dealerName ? ` • ${staff.dealerName}` : ""}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-sm font-medium">Đại lý *</label>
            <select
              {...register("dealerId", { valueAsNumber: true })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              disabled={metadataLoading}
            >
              <option value={0}>Chọn đại lý</option>
              {dealerOptions.map((dealer) => (
                <option key={dealer.id} value={dealer.id}>
                  {dealer.dealerName}
                </option>
              ))}
            </select>
            {errors.dealerId && (
              <p className="text-sm text-destructive mt-1">{errors.dealerId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Sản phẩm quan tâm *</label>
            <select
              {...register("productId", { valueAsNumber: true })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              disabled={metadataLoading}
            >
              <option value={0}>Chọn sản phẩm</option>
              {productOptions.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.productName}
                  {product.version ? ` • ${product.version}` : ""}
                </option>
              ))}
            </select>
            {errors.productId && (
              <p className="text-sm text-destructive mt-1">{errors.productId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Thời gian hẹn *</label>
            <Input
              type="datetime-local"
              {...register("appointmentTime")}
              className="mt-1"
            />
            {errors.appointmentTime && (
              <p className="text-sm text-destructive mt-1">{errors.appointmentTime.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Ghi chú</label>
            <textarea
              {...register("notes")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết lịch hẹn"
        open={viewMode === "detail" && selectedAppointment !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedAppointment(null);
        }}
        footer={
          selectedAppointment?.status !== "CANCELLED" && selectedAppointment?.status !== "COMPLETED" ? (
            <Button onClick={() => selectedAppointment && handleEdit(selectedAppointment)}>
              <Edit className="mr-2 h-4 w-4" />
              Sửa
            </Button>
          ) : null
        }
      >
        {selectedAppointment && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">ID</label>
              <p className="text-lg font-semibold">#{selectedAppointment.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedAppointment.status)}`}>
                  {selectedAppointment.status}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Ngày giờ hẹn</label>
              <p>{formatDateTime(selectedAppointment.appointmentTime)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Khách hàng</label>
              <p>{selectedAppointment.customerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Nhân viên</label>
              <p>{selectedAppointment.staffName || "Chưa phân công"}</p>
            </div>
            {selectedAppointment.productName && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Sản phẩm</label>
                <p>{selectedAppointment.productName}</p>
              </div>
            )}
          </div>
        )}
      </EntityModal>
    </div>
  );
}
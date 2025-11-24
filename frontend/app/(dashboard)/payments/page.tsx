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
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Check, X } from "lucide-react";

interface Payment {
  id: number;
  orderId: number;
  customerId: number;
  customerName: string;
  amount: number;
  paymentMethod: string;
  paymentType: string;
  status: string;
  referenceNumber?: string;
  paymentDate: string;
  createdAt: string;
}

type PaymentApiResponse = Payment & { paymentId?: number };

const paymentSchema = z.object({
  orderId: z.number().min(1, "Vui lòng chọn đơn hàng"),
  amount: z.number().min(0.01, "Số tiền phải lớn hơn 0"),
  paymentMethod: z.string().min(1, "Vui lòng chọn phương thức thanh toán"),
  paymentType: z.string().min(1, "Vui lòng chọn loại thanh toán"),
  referenceNumber: z.string().optional(),
  paymentDate: z.string().min(1, "Vui lòng chọn ngày thanh toán"),
});

type PaymentForm = z.infer<typeof paymentSchema>;

const paymentMethodOptions = [
  { value: "CASH", label: "Tiền mặt" },
  { value: "BANK_TRANSFER", label: "Chuyển khoản" },
  { value: "CREDIT_CARD", label: "Thẻ tín dụng" },
  { value: "COMPANY_TRANSFER", label: "Chuyển khoản công ty" },
  { value: "QR_CODE", label: "Quét mã QR" },
  { value: "INSTALLMENT", label: "Thanh toán trả góp" },
];

const paymentTypeOptions = [
  { value: "ORDER_PAYMENT", label: "Thanh toán đơn hàng" },
  { value: "DEPOSIT", label: "Đặt cọc" },
  { value: "INSTALLMENT", label: "Trả góp" },
  { value: "FINAL_PAYMENT", label: "Thanh toán cuối" },
];

export default function PaymentsPage() {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [selectedPaymentId, setSelectedPaymentId] = useState<number | null>(null); 
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<PaymentForm>({
    resolver: zodResolver(paymentSchema),
  });

  useEffect(() => {
    fetchPayments();
  }, [page, search]);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("referenceNumber", search);
      }
      const response = await apiClient.get<PaginatedResponse<PaymentApiResponse>>(
        `/payments?${params.toString()}`
      );
      const normalizedPayments = response.data.content.map((payment) => ({
        ...payment,
        id: payment.id ?? payment.paymentId ?? 0,
      }));
      setPayments(normalizedPayments);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching payments:", error);
      toast.error("Không thể tải danh sách thanh toán");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedPayment(null);
    reset({
      orderId: 0,
      amount: 0,
      paymentMethod: paymentMethodOptions[0]?.value ?? "",
      paymentType: paymentTypeOptions[0]?.value ?? "",
      referenceNumber: "",
      paymentDate: new Date().toISOString().slice(0, 10),
    });
    setViewMode("create");
  };

  const handleEdit = (payment: Payment) => {
    console.log("🔍 Selected payment:", payment);
    setSelectedPayment(payment);
    const paymentId = payment.id ?? (payment as unknown as { paymentId?: number }).paymentId;
    setSelectedPaymentId(paymentId ?? null);

    const paymentCopy = { ...payment };
    setSelectedPayment(paymentCopy);
    
    // Set form values
    setValue("orderId", Number(payment.orderId));
    setValue("amount", Number(payment.amount));
    setValue("paymentMethod", payment.paymentMethod);
    setValue("paymentType", payment.paymentType);
    setValue("referenceNumber", payment.referenceNumber || "");
    setValue(
      "paymentDate",
      payment.paymentDate
        ? payment.paymentDate.slice(0, 10)
        : new Date().toISOString().slice(0, 10)
    );
    
    setViewMode("edit");
  };

  const handleView = (payment: Payment) => {
    setSelectedPayment(payment);
    setViewMode("detail");
  };

  const handleDelete = async (payment: Payment) => {
    if (!confirm(`Bạn có chắc muốn xóa thanh toán #${payment.id}?`)) {
      return;
    }
    try {
      await apiClient.delete(`/payments/${payment.id}`);
      toast.success("Xóa thành công!");
      fetchPayments();
    } catch (error) {
      console.error("Error deleting payment:", error);
      toast.error("Không thể xóa thanh toán");
    }
  };

  const handleConfirm = async (payment: Payment) => {
    try {
      await apiClient.patch(`/payments/${payment.id}/confirm`);
      toast.success("Xác nhận thanh toán thành công!");
      fetchPayments();
    } catch (error) {
      console.error("Error confirming payment:", error);
      toast.error("Không thể xác nhận thanh toán");
    }
  };

  const handleRefund = async (payment: Payment) => {
    if (!confirm(`Bạn có chắc muốn hoàn tiền thanh toán #${payment.id}?`)) {
      return;
    }
    const reason = prompt("Lý do hoàn tiền:");
    try {
      await apiClient.patch(`/payments/${payment.id}/refund${reason ? `?reason=${encodeURIComponent(reason)}` : ""}`);
      toast.success("Hoàn tiền thành công!");
      fetchPayments();
    } catch (error) {
      console.error("Error refunding payment:", error);
      toast.error("Không thể hoàn tiền");
    }
  };

  const onSubmit = async (data: PaymentForm) => {
    try {
      console.log("📤 Form data:", data);
      
      if (viewMode === "create") {
        await apiClient.post("/payments", data);
        toast.success("Tạo thanh toán thành công!");
      } else if (viewMode === "edit") {
        if (!selectedPaymentId) {
          toast.error("Không tìm thấy thanh toán cần sửa");
          console.error("❌ selectedPaymentId is null:", {
            selectedPayment,
            selectedPaymentId,
          });
          return;
        }
        
        console.log("📤 Updating payment ID:", selectedPaymentId);
        await apiClient.put(`/payments/${selectedPaymentId}`, data); // ✅ DÙNG selectedPaymentId
        toast.success("Cập nhật thành công!");
      }
      
      setViewMode("list");
      setSelectedPayment(null);
      setSelectedPaymentId(null); // ✅ RESET
      reset();
      fetchPayments();
    } catch (error: any) {
      console.error("❌ Error saving payment:", error);
      console.error("❌ Response:", error.response?.data);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu thanh toán";
      toast.error(errorMessage);
    }
  };

  const handleModalClose = () => {
    setViewMode("list");
    setSelectedPayment(null);
    setSelectedPaymentId(null); // ✅ RESET
    reset();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Thanh toán</h1>
          <p className="text-muted-foreground">Quản lý thanh toán đơn hàng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchPayments} disabled={loading}>
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
              placeholder="Tìm kiếm theo mã tham chiếu..."
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
            {payments.map((payment) => (
              <Card key={payment.id} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">#{payment.id}</CardTitle>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(payment.status)}`}>
                      {payment.status}
                    </span>
                  </div>
                  <CardDescription>
                    {formatDate(payment.paymentDate)}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Khách hàng</span>
                      <span className="font-medium">{payment.customerName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Số tiền</span>
                      <span className="font-bold text-primary">
                        {formatCurrency(payment.amount)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Phương thức</span>
                      <span className="font-medium">{payment.paymentMethod}</span>
                    </div>
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <Button variant="outline" size="sm" onClick={() => handleView(payment)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    {payment.status === "PENDING" && (
                      <Button variant="outline" size="sm" onClick={() => handleConfirm(payment)}>
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                    {payment.status === "COMPLETED" && (
                      <Button variant="outline" size="sm" onClick={() => handleRefund(payment)}>
                        <X className="h-4 w-4" />
                      </Button>
                    )}
                    <Button variant="outline" size="sm" onClick={() => handleEdit(payment)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(payment)}
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
        title={viewMode === "create" ? "Thêm thanh toán mới" : "Sửa thanh toán"}
        open={viewMode === "create" || viewMode === "edit"}
        onClose={handleModalClose}
        footer={
          <>
            <Button variant="outline" onClick={handleModalClose}>
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
        <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
          <div>
            <label className="text-sm font-medium">Đơn hàng ID *</label>
            <Input
              type="number"
              {...register("orderId", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.orderId && (
              <p className="text-sm text-destructive mt-1">{errors.orderId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Số tiền (VND) *</label>
            <Input
              type="number"
              step="0.01"
              {...register("amount", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.amount && (
              <p className="text-sm text-destructive mt-1">{errors.amount.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Phương thức thanh toán *</label>
            <select
              {...register("paymentMethod")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn phương thức</option>
              {paymentMethodOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.paymentMethod && (
              <p className="text-sm text-destructive mt-1">{errors.paymentMethod.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Loại thanh toán *</label>
            <select
              {...register("paymentType")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn loại</option>
              {paymentTypeOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.paymentType && (
              <p className="text-sm text-destructive mt-1">{errors.paymentType.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Ngày thanh toán *</label>
            <Input type="date" {...register("paymentDate")} className="mt-1" />
            {errors.paymentDate && (
              <p className="text-sm text-destructive mt-1">{errors.paymentDate.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Mã tham chiếu</label>
            <Input {...register("referenceNumber")} className="mt-1" />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết thanh toán"
        open={viewMode === "detail" && selectedPayment !== null}
        onClose={handleModalClose}
        footer={
          <Button onClick={() => selectedPayment && handleEdit(selectedPayment)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedPayment && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">ID</label>
              <p className="text-lg font-semibold">#{selectedPayment.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedPayment.status)}`}>
                  {selectedPayment.status}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Khách hàng</label>
              <p>{selectedPayment.customerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Số tiền</label>
              <p className="text-2xl font-bold text-primary">
                {formatCurrency(selectedPayment.amount)}
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Phương thức</label>
              <p>{selectedPayment.paymentMethod}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Loại</label>
              <p>{selectedPayment.paymentType}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
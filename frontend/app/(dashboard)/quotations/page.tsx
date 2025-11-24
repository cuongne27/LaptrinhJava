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
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Send, Check, X, FileText, Download } from "lucide-react";

interface Quotation {
  id: number;
  quotationNumber: string;
  quotationDate: string;
  validUntil: string;
  status: string;
  basePrice: number;
  vat: number;
  registrationFee: number;
  discountAmount: number;
  totalPrice: number;
  productId: number;
  productName: string;
  customerId: number;
  customerName: string;
  salesPersonId: number;
  salesPersonName: string;
  dealerId: number;
  dealerName: string;
  isExpired?: boolean;
  canConvertToOrder?: boolean;
}

const quotationSchema = z.object({
  productId: z.number().min(1, "Vui lòng chọn sản phẩm"),
  customerId: z.number().min(1, "Vui lòng chọn khách hàng"),
  dealerId: z.number().min(1, "Vui lòng chọn đại lý"),
  salesPersonId: z.number().optional(),
  quotationDate: z.string().optional(),
  validUntil: z.string().optional(),
  basePrice: z.number().min(0.01, "Giá phải lớn hơn 0"),
  registrationFee: z.number().optional(),
  notes: z.string().optional(),
  termsAndConditions: z.string().optional(),
});

type QuotationForm = z.infer<typeof quotationSchema>;

export default function QuotationsPage() {
  const [quotations, setQuotations] = useState<Quotation[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedQuotation, setSelectedQuotation] = useState<Quotation | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<QuotationForm>({
    resolver: zodResolver(quotationSchema),
  });

  useEffect(() => {
    fetchQuotations();
  }, [page, search, refreshTrigger]);

  const fetchQuotations = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
        sortBy: "quotationDate",
        sortDirection: "desc",
      });
      const response = await apiClient.get<PaginatedResponse<Quotation>>(
        `/quotations?${params.toString()}`
      );
      setQuotations(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching quotations:", error);
      toast.error("Không thể tải danh sách báo giá");
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
    reset({
      productId: 0,
      customerId: 0,
      dealerId: 0,
      basePrice: 0,
      registrationFee: 0,
    });
    setSelectedQuotation(null);
    setViewMode("create");
  };

  const handleEdit = (quotation: Quotation) => {
    reset({
      productId: quotation.productId,
      customerId: quotation.customerId,
      dealerId: quotation.dealerId,
      salesPersonId: quotation.salesPersonId,
      basePrice: quotation.basePrice,
      registrationFee: quotation.registrationFee || 0,
      quotationDate: quotation.quotationDate,
      validUntil: quotation.validUntil,
    });
    setSelectedQuotation(quotation);
    setViewMode("edit");
  };

  const handleView = (quotation: Quotation) => {
    setSelectedQuotation(quotation);
    setViewMode("detail");
  };

  const handleDelete = async (quotation: Quotation) => {
    // Kiểm tra trạng thái trước khi xóa
    if (quotation.status === "ACCEPTED" || quotation.status === "CONVERTED") {
      toast.error("Không thể xóa báo giá đã được chấp nhận hoặc đã chuyển thành đơn hàng");
      return;
    }
    
    // Kiểm tra có sales order liên quan không
    if (quotation.salesPersonId || quotation.canConvertToOrder) {
      const confirmAdvanced = confirm(
        `Báo giá "${quotation.quotationNumber}" có thể có dữ liệu liên quan.\n\n` +
        `Bạn có chắc chắn muốn xóa? Thao tác này không thể hoàn tác.`
      );
      if (!confirmAdvanced) return;
    } else {
      if (!confirm(`Bạn có chắc muốn xóa báo giá "${quotation.quotationNumber}"?`)) {
        return;
      }
    }
    
    try {
      await apiClient.delete(`/quotations/${quotation.id}`);
      toast.success("Xóa thành công!");
      fetchQuotations();
    } catch (error: any) {
      console.error("Error deleting quotation:", error);
      
      // Xử lý các loại lỗi cụ thể
      if (error.response?.status === 409) {
        toast.error("Không thể xóa: Báo giá đang được sử dụng trong đơn hàng hoặc giao dịch khác");
      } else if (error.response?.status === 400) {
        const errorMessage = error.response?.data?.message || error.response?.data?.error || "Yêu cầu không hợp lệ";
        toast.error(errorMessage);
      } else if (error.response?.status === 500) {
        toast.error(
          "Không thể xóa báo giá này. Có thể do:\n" +
          "• Đã có đơn hàng liên quan\n" +
          "• Đang được sử dụng trong khuyến mãi\n" +
          "• Có ràng buộc dữ liệu khác\n\n" +
          "Vui lòng liên hệ quản trị viên."
        );
      } else {
        const errorMessage = error.response?.data?.message || error.response?.data?.error || "Không thể xóa báo giá";
        toast.error(errorMessage);
      }
    }
  };

  const handleSend = async (quotation: Quotation) => {
    try {
      await apiClient.post(`/quotations/${quotation.id}/send`);
      toast.success("Gửi báo giá thành công!");
      fetchQuotations();
    } catch (error: any) {
      console.error("Error sending quotation:", error);
      const errorMessage = error.response?.data?.message || "Không thể gửi báo giá";
      toast.error(errorMessage);
    }
  };

  const handleAccept = async (quotation: Quotation) => {
    try {
      await apiClient.post(`/quotations/${quotation.id}/accept`);
      toast.success("Chấp nhận báo giá thành công!");
      fetchQuotations();
    } catch (error: any) {
      console.error("Error accepting quotation:", error);
      const errorMessage = error.response?.data?.message || "Không thể chấp nhận báo giá";
      toast.error(errorMessage);
    }
  };

  const handleReject = async (quotation: Quotation) => {
    try {
      await apiClient.post(`/quotations/${quotation.id}/reject`);
      toast.success("Từ chối báo giá thành công!");
      fetchQuotations();
    } catch (error: any) {
      console.error("Error rejecting quotation:", error);
      const errorMessage = error.response?.data?.message || "Không thể từ chối báo giá";
      toast.error(errorMessage);
    }
  };

  const handleConvertToOrder = async (quotation: Quotation) => {
    if (!confirm(`Chuyển báo giá "${quotation.quotationNumber}" thành đơn hàng?`)) {
      return;
    }
    try {
      await apiClient.post(`/quotations/${quotation.id}/convert-to-order`);
      toast.success("Chuyển thành đơn hàng thành công!");
      fetchQuotations();
    } catch (error: any) {
      console.error("Error converting quotation:", error);
      const errorMessage = error.response?.data?.message || "Không thể chuyển thành đơn hàng";
      toast.error(errorMessage);
    }
  };

  const handleExportPdf = async (quotation: Quotation) => {
    try {
      const response = await apiClient.get(`/quotations/${quotation.id}/export-pdf`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `quotation-${quotation.quotationNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success("Xuất PDF thành công!");
    } catch (error: any) {
      console.error("Error exporting PDF:", error);
      const errorMessage = error.response?.data?.message || "Không thể xuất PDF";
      toast.error(errorMessage);
    }
  };

  const onSubmit = async (data: QuotationForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/quotations", data);
        toast.success("Tạo báo giá thành công!");
      } else if (viewMode === "edit" && selectedQuotation) {
        await apiClient.put(`/quotations/${selectedQuotation.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchQuotations();
    } catch (error: any) {
      console.error("Error saving quotation:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu báo giá";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Báo giá</h1>
          <p className="text-muted-foreground">Quản lý báo giá cho khách hàng</p>
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
              placeholder="Tìm kiếm báo giá..."
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
            {quotations.map((quotation) => (
              <Card key={quotation.id} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{quotation.quotationNumber}</CardTitle>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(quotation.status)}`}>
                      {quotation.status}
                    </span>
                  </div>
                  <CardDescription>
                    {formatDate(quotation.quotationDate)} - {formatDate(quotation.validUntil)}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Khách hàng</span>
                      <span className="font-medium">{quotation.customerName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Sản phẩm</span>
                      <span className="font-medium">{quotation.productName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Tổng giá</span>
                      <span className="font-bold text-primary">
                        {formatCurrency(quotation.totalPrice)}
                      </span>
                    </div>
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <Button variant="outline" size="sm" onClick={() => handleView(quotation)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    {quotation.status === "DRAFT" && (
                      <>
                        <Button variant="outline" size="sm" onClick={() => handleEdit(quotation)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleSend(quotation)}>
                          <Send className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDelete(quotation)}
                          title="Chỉ có thể xóa báo giá nháp"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </>
                    )}
                    {quotation.status === "SENT" && (
                      <>
                        <Button variant="outline" size="sm" onClick={() => handleAccept(quotation)}>
                          <Check className="h-4 w-4" />
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleReject(quotation)}>
                          <X className="h-4 w-4" />
                        </Button>
                      </>
                    )}
                    {quotation.canConvertToOrder && (
                      <Button variant="outline" size="sm" onClick={() => handleConvertToOrder(quotation)}>
                        <FileText className="h-4 w-4" />
                      </Button>
                    )}
                    <Button variant="outline" size="sm" onClick={() => handleExportPdf(quotation)}>
                      <Download className="h-4 w-4" />
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
        title={viewMode === "create" ? "Thêm báo giá mới" : "Sửa báo giá"}
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
            <label className="text-sm font-medium">Sản phẩm *</label>
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
            <label className="text-sm font-medium">Khách hàng *</label>
            <Input
              type="number"
              {...register("customerId", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.customerId && (
              <p className="text-sm text-destructive mt-1">{errors.customerId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Đại lý *</label>
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
            <label className="text-sm font-medium">Phí đăng ký (VND)</label>
            <Input
              type="number"
              step="0.01"
              {...register("registrationFee", { valueAsNumber: true })}
              className="mt-1"
            />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết báo giá"
        open={viewMode === "detail" && selectedQuotation !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedQuotation(null);
        }}
        footer={
          <>
            {selectedQuotation?.status === "DRAFT" && (
              <Button onClick={() => selectedQuotation && handleEdit(selectedQuotation)}>
                <Edit className="mr-2 h-4 w-4" />
                Sửa
              </Button>
            )}
            <Button onClick={() => selectedQuotation && handleExportPdf(selectedQuotation)}>
              <Download className="mr-2 h-4 w-4" />
              Xuất PDF
            </Button>
          </>
        }
      >
        {selectedQuotation && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Mã báo giá</label>
              <p className="text-lg font-semibold">{selectedQuotation.quotationNumber}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedQuotation.status)}`}>
                  {selectedQuotation.status}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Ngày báo giá</label>
              <p>{formatDate(selectedQuotation.quotationDate)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Có hiệu lực đến</label>
              <p>{formatDate(selectedQuotation.validUntil)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Khách hàng</label>
              <p>{selectedQuotation.customerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Sản phẩm</label>
              <p>{selectedQuotation.productName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Giá cơ bản</label>
              <p>{formatCurrency(selectedQuotation.basePrice)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tổng giá</label>
              <p className="text-2xl font-bold text-primary">
                {formatCurrency(selectedQuotation.totalPrice)}
              </p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
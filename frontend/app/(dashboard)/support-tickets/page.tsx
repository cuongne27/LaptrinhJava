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
import { formatDateTime, getStatusColor, getPriorityColor } from "@/lib/utils";
import type { PaginatedResponse } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Check, X, UserPlus } from "lucide-react";

type ViewMode = "list" | "create" | "edit" | "detail";

interface SupportTicket {
  id: number;
  ticketNumber: string;
  description: string | null; // Cập nhật để chấp nhận null
  status: string;
  priority: string;
  category: string;
  customerId: number;
  customerName: string;
  assignedUserId?: number;
  assignedUserName?: string;
  salesOrderId?: number;
  vehicleId?: string;
  createdAt: string;
}

const ticketSchema = z.object({
  customerId: z.number().min(1, "Vui lòng chọn khách hàng"),
  description: z.string().min(1, "Mô tả không được để trống"),
  priority: z.string().min(1, "Vui lòng chọn độ ưu tiên"),
  category: z.string().min(1, "Vui lòng chọn danh mục"),
  salesOrderId: z.number().optional(),
  vehicleId: z.string().optional(),
});

type TicketForm = z.infer<typeof ticketSchema>;

export default function SupportTicketsPage() {
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null);
  const [viewMode, setViewMode] = useState<ViewMode>("list");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<TicketForm>({
    resolver: zodResolver(ticketSchema),
  });

  useEffect(() => {
    fetchTickets();
  }, [page, search]);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<SupportTicket>>(
        `/support-tickets?${params.toString()}`
      );
      setTickets(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching tickets:", error);
      toast.error("Không thể tải danh sách tickets");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    reset({
      customerId: 0,
      description: "",
      priority: "",
      category: "",
    });
    setSelectedTicket(null);
    setViewMode("create");
  };

  const handleEdit = (ticket: SupportTicket) => {
    reset({
      customerId: ticket.customerId,
      description: ticket.description ?? "", // Thêm ?? ""
      priority: ticket.priority,
      category: ticket.category,
      salesOrderId: ticket.salesOrderId,
      vehicleId: ticket.vehicleId,
    });
    setSelectedTicket(ticket);
    setViewMode("edit");
  };

  const handleView = (ticket: SupportTicket) => {
    setSelectedTicket(ticket);
    setViewMode("detail");
  };

  const handleDelete = async (ticket: SupportTicket) => {
    if (!confirm(`Bạn có chắc muốn xóa ticket "${ticket.ticketNumber}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/support-tickets/${ticket.id}`);
      toast.success("Xóa thành công!");
      fetchTickets();
    } catch (error) {
      console.error("Error deleting ticket:", error);
      toast.error("Không thể xóa ticket");
    }
  };

  const handleAssign = async (ticket: SupportTicket) => {
    try {
      // await apiClient.patch(`/support-tickets/${ticket.id}/assign-me`);
      // toast.success("Gán ticket thành công!");
      fetchTickets();
    } catch (error) {
      console.error("Error assigning ticket:", error);
      toast.error("Không thể gán ticket");
    }
  };

  const handleClose = async (ticket: SupportTicket) => {
    try {
      await apiClient.patch(`/support-tickets/${ticket.id}/close`);
      toast.success("Đóng ticket thành công!");
      fetchTickets();
    } catch (error) {
      console.error("Error closing ticket:", error);
      toast.error("Không thể đóng ticket");
    }
  };

  const handleReopen = async (ticket: SupportTicket) => {
    try {
      await apiClient.patch(`/support-tickets/${ticket.id}/reopen`);
      toast.success("Mở lại ticket thành công!");
      fetchTickets();
    } catch (error) {
      console.error("Error reopening ticket:", error);
      toast.error("Không thể mở lại ticket");
    }
  };

  const onSubmit = async (data: TicketForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/support-tickets", data);
        toast.success("Tạo ticket thành công!");
      } else if (viewMode === "edit" && selectedTicket) {
        await apiClient.put(`/support-tickets/${selectedTicket.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchTickets();
    } catch (error: any) {
      console.error("Error saving ticket:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu ticket";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Hỗ trợ</h1>
          <p className="text-muted-foreground">Quản lý tickets hỗ trợ khách hàng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchTickets} disabled={loading}>
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
              placeholder="Tìm kiếm ticket..."
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
          <div className="space-y-4">
            {tickets.map((ticket) => (
              <Card key={ticket.ticketNumber}>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 p-4">
                  <div className="space-y-1">
                    <CardTitle className="text-lg">
                      #{ticket.id} - {ticket.customerName}
                    </CardTitle>
                    <CardDescription className="text-sm">
                      {/* SỬA LỖI SUBSTRING: Dùng Nullish Coalescing (?? "") để đảm bảo description là chuỗi */}
                      {(ticket.description ?? "").substring(0, 100)}...
                    </CardDescription>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getPriorityColor(ticket.priority)}`}>
                      {ticket.priority}
                    </span>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(ticket.status)}`}>
                      {ticket.status}
                    </span>
                  </div>
                </CardHeader>
                <CardContent className="flex items-center justify-between p-4 pt-0">
                  <div className="text-sm text-muted-foreground space-y-1">
                    <div>
                      <strong>Danh mục:</strong> {ticket.category}
                    </div>
                    {ticket.assignedUserName && (
                      <div>
                        <strong>Phụ trách:</strong> {ticket.assignedUserName}
                      </div>
                    )}
                    <div>
                      <strong>Ngày tạo:</strong> {formatDateTime(ticket.createdAt)}
                    </div>
                  </div>
                  <div className="flex space-x-2">
                    <Button variant="outline" size="sm" onClick={() => handleView(ticket)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    {!ticket.assignedUserId && (
                      <Button variant="secondary" size="sm" onClick={() => handleAssign(ticket)}>
                        <UserPlus className="h-4 w-4" />
                      </Button>
                    )}
                    {ticket.status === "OPEN" && (
                      <Button variant="default" size="sm" onClick={() => handleClose(ticket)}>
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                    {ticket.status === "CLOSED" && (
                      <Button variant="secondary" size="sm" onClick={() => handleReopen(ticket)}>
                        <RefreshCw className="h-4 w-4" />
                      </Button>
                    )}
                    <Button variant="outline" size="sm" onClick={() => handleEdit(ticket)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(ticket)}
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
        title={viewMode === "create" ? "Thêm ticket mới" : "Sửa ticket"}
        open={viewMode === "create" || viewMode === "edit"}
        onClose={() => setViewMode("list")}
        footer={
          <>
            <Button variant="outline" onClick={() => setViewMode("list")}>
              Hủy
            </Button>
            <Button
              disabled={isSubmitting}
              onClick={handleSubmit(onSubmit)}
            >
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Khách hàng ID *</label>
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
            <label className="text-sm font-medium">Mô tả *</label>
            <textarea
              {...register("description")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            />
            {errors.description && (
              <p className="text-sm text-destructive mt-1">{errors.description.message}</p>
            )}
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Độ ưu tiên *</label>
              <select
                {...register("priority")}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              >
                <option value="">Chọn độ ưu tiên</option>
                <option value="LOW">Thấp</option>
                <option value="MEDIUM">Trung bình</option>
                <option value="HIGH">Cao</option>
                <option value="URGENT">Khẩn cấp</option>
              </select>
              {errors.priority && (
                <p className="text-sm text-destructive mt-1">{errors.priority.message}</p>
              )}
            </div>
            <div>
              <label className="text-sm font-medium">Danh mục *</label>
              <select
                {...register("category")}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
              >
                <option value="">Chọn danh mục</option>
                <option value="TECHNICAL">Kỹ thuật</option>
                <option value="BILLING">Thanh toán</option>
                <option value="SALES">Bán hàng</option>
                <option value="OTHER">Khác</option>
              </select>
              {errors.category && (
                <p className="text-sm text-destructive mt-1">{errors.category.message}</p>
              )}
            </div>
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết ticket"
        open={viewMode === "detail" && selectedTicket !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedTicket(null);
        }}
        footer={
          <>
            {selectedTicket && !selectedTicket.assignedUserId && (
              <Button variant="secondary" onClick={() => selectedTicket && handleAssign(selectedTicket)}>
                <UserPlus className="mr-2 h-4 w-4" />
                Gán cho tôi
              </Button>
            )}
            <Button onClick={() => selectedTicket && handleEdit(selectedTicket)}>
              <Edit className="mr-2 h-4 w-4" />
              Chỉnh sửa
            </Button>
          </>
        }
      >
        {selectedTicket && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Số ticket</label>
              <p className="text-lg font-semibold">{selectedTicket.ticketNumber}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tiêu đề</label>
              <p>{selectedTicket.description ?? "Không có tiêu đề"}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Mô tả</label>
              <p>{selectedTicket.description ?? "Không có mô tả"}</p>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
                <p>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedTicket.status)}`}>
                    {selectedTicket.status}
                  </span>
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Độ ưu tiên</label>
                <p>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${getPriorityColor(selectedTicket.priority)}`}>
                    {selectedTicket.priority}
                  </span>
                </p>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Khách hàng</label>
                <p>{selectedTicket.customerName}</p>
              </div>
              {selectedTicket.assignedUserName && (
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Người phụ trách</label>
                  <p>{selectedTicket.assignedUserName}</p>
                </div>
              )}
            </div>
            <div>
                <label className="text-sm font-medium text-muted-foreground">Ngày tạo</label>
                <p>{formatDateTime(selectedTicket.createdAt)}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
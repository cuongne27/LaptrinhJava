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

interface SupportTicket {
  id?: number;
  ticketId?: number; // Backend might return this instead
  ticketNumber: string;
  title: string;
  description: string;
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
  title: z.string().min(1, "Tiêu đề không được để trống"),
  description: z.string().min(1, "Mô tả không được để trống"),
  priority: z.string().min(1, "Vui lòng chọn độ ưu tiên"),
  category: z.string().min(1, "Vui lòng chọn danh mục"),
  salesOrderId: z.number().optional(),
  vehicleId: z.string().optional(),
});

type TicketForm = z.infer<typeof ticketSchema>;

// Helper functions for display labels
const getPriorityLabel = (priority: string) => {
  const labels: Record<string, string> = {
    LOW: "Thấp",
    MEDIUM: "Trung bình",
    HIGH: "Cao",
    URGENT: "Khẩn cấp",
  };
  return labels[priority] || priority;
};

const getCategoryLabel = (category: string) => {
  const labels: Record<string, string> = {
    TECHNICAL: "Kỹ thuật",
    BILLING: "Thanh toán",
    SALES: "Bán hàng",
    WARRANTY: "Bảo hành",
    OTHER: "Khác",
  };
  return labels[category] || category;
};

export default function SupportTicketsPage() {
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail" | "assign">("list");
  const [assignUserId, setAssignUserId] = useState<string>("");

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<TicketForm>({
    resolver: zodResolver(ticketSchema),
    defaultValues: {
      customerId: 0,
      subject: "",
      description: "",
      priority: "",
      category: "",
    }
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
      
      // Log để check kiểu dữ liệu
      if (response.data.content.length > 0) {
        console.log("First ticket:", response.data.content[0]);
        console.log("First ticket ID type:", typeof response.data.content[0].id);
      }
      
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
      title: "",
      description: "",
      priority: "",
      category: "",
      salesOrderId: undefined,
      vehicleId: undefined,
    });
    setSelectedTicket(null);
    setViewMode("create");
  };

  const handleEdit = (ticket: SupportTicket) => {
    console.log("Editing ticket:", ticket);
    
    // Reset the form first to clear any previous state
    reset({
      customerId: ticket.customerId,
      title: ticket.title,
      description: ticket.description,
      priority: ticket.priority, // This should already be the enum value like "MEDIUM"
      category: ticket.category, // This should already be the enum value like "TECHNICAL"
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
    setSelectedTicket(ticket);
    setAssignUserId("");
    setViewMode("assign");
  };

  const handleAssignSubmit = async () => {
    if (!selectedTicket || !assignUserId) {
      toast.error("Vui lòng nhập User ID");
      return;
    }
    try {
      await apiClient.patch(`/support-tickets/${selectedTicket.id}/assign`, null, {
        params: { userId: assignUserId }
      });
      toast.success("Phân công thành công!");
      setViewMode("list");
      setAssignUserId("");
      setSelectedTicket(null);
      fetchTickets();
    } catch (error: any) {
      console.error("Error assigning ticket:", error);
      const errorMessage = error.response?.data?.message || error.response?.data?.error || "Không thể phân công ticket";
      toast.error(errorMessage);
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
    console.log("Form data being submitted:", data);
    
    // Ensure we're sending the correct data types
    const payload = {
      customerId: Number(data.customerId),
      title: data.title.trim(),
      description: data.description.trim(),
      priority: String(data.priority), // Explicitly cast to string
      category: String(data.category), // Explicitly cast to string
      salesOrderId: data.salesOrderId ? Number(data.salesOrderId) : undefined,
      vehicleId: data.vehicleId?.trim() || undefined,
    };
    
    // Remove undefined values
    Object.keys(payload).forEach(key => {
      if (payload[key as keyof typeof payload] === undefined) {
        delete payload[key as keyof typeof payload];
      }
    });
    
    console.log("Payload being sent:", payload);
    console.log("Payload as JSON:", JSON.stringify(payload, null, 2));
    
    try {
      if (viewMode === "create") {
        const response = await apiClient.post("/support-tickets", payload, {
          headers: {
            'Content-Type': 'application/json',
          },
        });
        console.log("Response:", response);
        toast.success("Tạo ticket thành công!");
      } else if (viewMode === "edit" && selectedTicket) {
        const response = await apiClient.put(`/support-tickets/${selectedTicket.id}`, payload, {
          headers: {
            'Content-Type': 'application/json',
          },
        });
        console.log("Response:", response);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchTickets();
    } catch (error: any) {
      console.error("Error saving ticket:", error);
      console.error("Error response:", error.response?.data);
      console.error("Error config:", error.config);
      console.error("Request data sent:", error.config?.data);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu ticket";
      toast.error(errorMessage);
    }
  };

  const handleModalClose = () => {
    setViewMode("list");
    reset();
    setSelectedTicket(null);
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
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {tickets.map((ticket) => (
              <Card key={ticket.id} className="flex flex-col hover:shadow-lg transition-shadow">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div className="flex-1 min-w-0">
                      <CardTitle className="text-base font-semibold truncate">
                        {ticket.ticketNumber}
                      </CardTitle>
                      <p className="text-sm text-muted-foreground line-clamp-2 mt-1">
                        {ticket.title}
                      </p>
                    </div>
                    <div className="flex flex-col gap-1 shrink-0">
                      <span className={`px-2 py-0.5 rounded text-xs font-medium whitespace-nowrap ${getStatusColor(ticket.status)}`}>
                        {ticket.status}
                      </span>
                      <span className={`px-2 py-0.5 rounded text-xs font-medium whitespace-nowrap ${getPriorityColor(ticket.priority)}`}>
                        {getPriorityLabel(ticket.priority)}
                      </span>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="flex-1 flex flex-col justify-between pt-0">
                  <div className="space-y-2 mb-3">
                    <div className="flex items-center gap-2 text-sm">
                      <span className="text-muted-foreground min-w-[80px]">Khách hàng</span>
                      <span className="font-medium truncate">{ticket.customerName}</span>
                    </div>
                    {ticket.assignedUserName && (
                      <div className="flex items-center gap-2 text-sm">
                        <span className="text-muted-foreground min-w-[80px]">Người phụ trách</span>
                        <span className="font-medium truncate">{ticket.assignedUserName}</span>
                      </div>
                    )}
                    <div className="flex items-center gap-2 text-sm">
                      <span className="text-muted-foreground min-w-[80px]">Danh mục</span>
                      <span className="font-medium truncate">{getCategoryLabel(ticket.category)}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-1 flex-wrap">
                    <Button 
                      variant="outline" 
                      size="sm" 
                      onClick={() => handleView(ticket)}
                      className="h-8 w-8 p-0"
                      title="Xem chi tiết"
                    >
                      <Eye className="h-4 w-4" />
                    </Button>
                    {!ticket.assignedUserId && (
                      <Button 
                        variant="outline" 
                        size="sm" 
                        onClick={() => handleAssign(ticket)}
                        className="h-8 w-8 p-0"
                        title="Phân công"
                      >
                        <UserPlus className="h-4 w-4" />
                      </Button>
                    )}
                    {ticket.status === "OPEN" && (
                      <Button 
                        variant="outline" 
                        size="sm" 
                        onClick={() => handleClose(ticket)}
                        className="h-8 w-8 p-0"
                        title="Đóng ticket"
                      >
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                    {ticket.status === "CLOSED" && (
                      <Button 
                        variant="outline" 
                        size="sm" 
                        onClick={() => handleReopen(ticket)}
                        className="h-8 w-8 p-0"
                        title="Mở lại ticket"
                      >
                        <RefreshCw className="h-4 w-4" />
                      </Button>
                    )}
                    <Button 
                      variant="outline" 
                      size="sm" 
                      onClick={() => handleEdit(ticket)}
                      className="h-8 w-8 p-0"
                      title="Chỉnh sửa"
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(ticket)}
                      className="h-8 w-8 p-0"
                      title="Xóa"
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
        onClose={handleModalClose}
        footer={
          <>
            <Button variant="outline" onClick={handleModalClose}>
              Hủy
            </Button>
            <Button
              type="submit"
              onClick={handleSubmit(onSubmit)}
              disabled={isSubmitting}
            >
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
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
            <label className="text-sm font-medium">Tiêu đề *</label>
            <Input {...register("title")} className="mt-1" />
            {errors.title && (
              <p className="text-sm text-destructive mt-1">{errors.title.message}</p>
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
              <option value="WARRANTY">Bảo hành</option>
              <option value="OTHER">Khác</option>
            </select>
            {errors.category && (
              <p className="text-sm text-destructive mt-1">{errors.category.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Sales Order ID (tùy chọn)</label>
            <Input
              type="number"
              {...register("salesOrderId", { 
                valueAsNumber: true,
                setValueAs: (v) => v === "" ? undefined : Number(v)
              })}
              className="mt-1"
              placeholder="Nhập ID đơn hàng nếu có"
            />
          </div>
          <div>
            <label className="text-sm font-medium">Vehicle ID (tùy chọn)</label>
            <Input
              {...register("vehicleId")}
              className="mt-1"
              placeholder="Nhập ID xe nếu có"
            />
          </div>
        </div>
      </EntityModal>

      {/* Assign Modal */}
      <EntityModal
        title="Phân công ticket"
        open={viewMode === "assign"}
        onClose={() => {
          setViewMode("list");
          setAssignUserId("");
          setSelectedTicket(null);
        }}
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => {
                setViewMode("list");
                setAssignUserId("");
                setSelectedTicket(null);
              }}
            >
              Hủy
            </Button>
            <Button onClick={handleAssignSubmit}>
              <UserPlus className="mr-2 h-4 w-4" />
              Phân công
            </Button>
          </>
        }
      >
        {selectedTicket && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Ticket</label>
              <p className="text-lg font-semibold">{selectedTicket.ticketNumber}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tiêu đề</label>
              <p>{selectedTicket.title}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Khách hàng</label>
              <p>{selectedTicket.customerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium">Nhập User ID để phân công *</label>
              <Input
                type="number"
                value={assignUserId}
                onChange={(e) => setAssignUserId(e.target.value)}
                placeholder="Ví dụ: 1, 2, 3..."
                className="mt-1"
                autoFocus
              />
              <p className="text-xs text-muted-foreground mt-1">
                Nhập ID của người dùng sẽ được phân công xử lý ticket này
              </p>
            </div>
          </div>
        )}
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
              <Button onClick={() => {
                setViewMode("assign");
              }}>
                <UserPlus className="mr-2 h-4 w-4" />
                Phân công
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
              <p>{selectedTicket.title}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Mô tả</label>
              <p>{selectedTicket.description}</p>
            </div>
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
                  {getPriorityLabel(selectedTicket.priority)}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Danh mục</label>
              <p>{getCategoryLabel(selectedTicket.category)}</p>
            </div>
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
        )}
      </EntityModal>
    </div>
  );
}
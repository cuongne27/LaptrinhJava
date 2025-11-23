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
import { formatDate } from "@/lib/utils";
import type { Customer, PaginatedResponse } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Mail, Phone } from "lucide-react";

const customerSchema = z.object({
  fullName: z.string().min(1, "Họ tên không được để trống"),
  phoneNumber: z.string().min(1, "Số điện thoại không được để trống"),
  email: z.string().email("Email không hợp lệ").optional().or(z.literal("")),
  address: z.string().optional(),
  customerType: z.string().optional(),
  history: z.string().optional(),
});

type CustomerForm = z.infer<typeof customerSchema>;

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [searchByEmail, setSearchByEmail] = useState("");
  const [searchByPhone, setSearchByPhone] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CustomerForm>({
    resolver: zodResolver(customerSchema),
  });

  useEffect(() => {
    fetchCustomers();
  }, [page, search]);

  const fetchCustomers = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<Customer>>(
        `/customers?${params.toString()}`
      );
      setCustomers(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching customers:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleFindByEmail = async () => {
    if (!searchByEmail.trim()) {
      toast.error("Vui lòng nhập email");
      return;
    }
    try {
      setLoading(true);
      const response = await apiClient.get<Customer>(`/customers/by-email?email=${encodeURIComponent(searchByEmail.trim())}`);
      if (response.data) {
        setCustomers([response.data]);
        setTotalPages(1);
        setPage(0);
        toast.success("Tìm thấy khách hàng!");
      }
    } catch (error: any) {
      console.error("Error finding customer by email:", error);
      toast.error(error.response?.data?.message || "Không tìm thấy khách hàng với email này");
      setCustomers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFindByPhone = async () => {
    if (!searchByPhone.trim()) {
      toast.error("Vui lòng nhập số điện thoại");
      return;
    }
    try {
      setLoading(true);
      const response = await apiClient.get<Customer>(`/customers/by-phone?phone=${encodeURIComponent(searchByPhone.trim())}`);
      if (response.data) {
        setCustomers([response.data]);
        setTotalPages(1);
        setPage(0);
        toast.success("Tìm thấy khách hàng!");
      }
    } catch (error: any) {
      console.error("Error finding customer by phone:", error);
      toast.error(error.response?.data?.message || "Không tìm thấy khách hàng với số điện thoại này");
      setCustomers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    reset({
      fullName: "",
      phoneNumber: "",
      email: "",
      address: "",
      customerType: "",
      history: "",
    });
    setSelectedCustomer(null);
    setViewMode("create");
  };

  const handleEdit = (customer: Customer) => {
    reset({
      fullName: customer.fullName,
      phoneNumber: customer.phoneNumber,
      email: customer.email || "",
      address: customer.address || "",
      customerType: customer.customerType || "",
      history: "",
    });
    setSelectedCustomer(customer);
    setViewMode("edit");
  };

  const handleView = (customer: Customer) => {
    setSelectedCustomer(customer);
    setViewMode("detail");
  };

  const handleDelete = async (customer: Customer) => {
    if (!confirm(`Bạn có chắc muốn xóa khách hàng "${customer.fullName}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/customers/${customer.id}`);
      toast.success("Xóa thành công!");
      fetchCustomers();
    } catch (error) {
      console.error("Error deleting customer:", error);
    }
  };

  const onSubmit = async (data: CustomerForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/customers", data);
        toast.success("Tạo khách hàng thành công!");
      } else if (viewMode === "edit" && selectedCustomer) {
        await apiClient.put(`/customers/${selectedCustomer.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchCustomers();
    } catch (error) {
      console.error("Error saving customer:", error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Khách hàng</h1>
          <p className="text-muted-foreground">Quản lý thông tin khách hàng</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchCustomers} disabled={loading}>
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
          <CardDescription>Tìm kiếm theo tên, email hoặc số điện thoại</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm khách hàng theo tên..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
                setSearchByEmail("");
                setSearchByPhone("");
              }}
              className="pl-9"
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium flex items-center gap-2">
                <Mail className="h-4 w-4" />
                Tìm theo Email
              </label>
              <div className="flex gap-2">
                <Input
                  type="email"
                  placeholder="Nhập email..."
                  value={searchByEmail}
                  onChange={(e) => {
                    setSearchByEmail(e.target.value);
                    setSearch("");
                    setSearchByPhone("");
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleFindByEmail();
                    }
                  }}
                />
                <Button onClick={handleFindByEmail} variant="outline">
                  Tìm
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium flex items-center gap-2">
                <Phone className="h-4 w-4" />
                Tìm theo Số điện thoại
              </label>
              <div className="flex gap-2">
                <Input
                  type="tel"
                  placeholder="Nhập số điện thoại..."
                  value={searchByPhone}
                  onChange={(e) => {
                    setSearchByPhone(e.target.value);
                    setSearch("");
                    setSearchByEmail("");
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleFindByPhone();
                    }
                  }}
                />
                <Button onClick={handleFindByPhone} variant="outline">
                  Tìm
                </Button>
              </div>
            </div>
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
              <CardTitle>Danh sách ({customers.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {customers.map((customer) => (
                  <div
                    key={customer.id}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary font-medium">
                          {customer.fullName.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <h3 className="font-semibold">{customer.fullName}</h3>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                            <div className="flex items-center gap-1">
                              <Mail className="h-3 w-3" />
                              {customer.email}
                            </div>
                            <div className="flex items-center gap-1">
                              <Phone className="h-3 w-3" />
                              {customer.phoneNumber}
                            </div>
                            {customer.customerType && (
                              <span className="px-2 py-1 text-xs bg-secondary rounded">
                                {customer.customerType}
                              </span>
                            )}
                            {customer.totalOrders !== undefined && (
                              <>
                                <span>•</span>
                                <span>{customer.totalOrders} đơn hàng</span>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button variant="outline" size="sm" onClick={() => handleView(customer)}>
                        <Eye className="mr-2 h-4 w-4" />
                        Xem
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleEdit(customer)}>
                        <Edit className="mr-2 h-4 w-4" />
                        Sửa
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDelete(customer)}
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Xóa
                      </Button>
                    </div>
                  </div>
                ))}
                {customers.length === 0 && (
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
        title={viewMode === "create" ? "Thêm khách hàng mới" : "Sửa khách hàng"}
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
            <label className="text-sm font-medium">Họ tên *</label>
            <Input {...register("fullName")} className="mt-1" />
            {errors.fullName && (
              <p className="text-sm text-destructive mt-1">{errors.fullName.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Số điện thoại *</label>
            <Input {...register("phoneNumber")} className="mt-1" />
            {errors.phoneNumber && (
              <p className="text-sm text-destructive mt-1">{errors.phoneNumber.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Email</label>
            <Input type="email" {...register("email")} className="mt-1" />
            {errors.email && (
              <p className="text-sm text-destructive mt-1">{errors.email.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Địa chỉ</label>
            <Input {...register("address")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Loại khách hàng</label>
            <select
              {...register("customerType")}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn loại</option>
              <option value="Individual">Cá nhân</option>
              <option value="Corporate">Doanh nghiệp</option>
              <option value="VIP">VIP</option>
              <option value="Regular">Thường</option>
            </select>
          </div>
          <div>
            <label className="text-sm font-medium">Lịch sử/Ghi chú</label>
            <textarea
              {...register("history")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết khách hàng"
        open={viewMode === "detail" && selectedCustomer !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedCustomer(null);
        }}
        footer={
          <Button onClick={() => selectedCustomer && handleEdit(selectedCustomer)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedCustomer && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Họ tên</label>
              <p className="text-lg font-semibold">{selectedCustomer.fullName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Số điện thoại</label>
              <p>{selectedCustomer.phoneNumber}</p>
            </div>
            {selectedCustomer.email && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Email</label>
                <p>{selectedCustomer.email}</p>
              </div>
            )}
            {selectedCustomer.address && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Địa chỉ</label>
                <p>{selectedCustomer.address}</p>
              </div>
            )}
            {selectedCustomer.customerType && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Loại khách hàng</label>
                <span className="inline-block px-2 py-1 text-xs bg-secondary rounded">
                  {selectedCustomer.customerType}
                </span>
              </div>
            )}
            <div className="grid grid-cols-2 gap-4 pt-4 border-t">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Tổng đơn hàng</label>
                <p className="text-2xl font-bold">{selectedCustomer.totalOrders || 0}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground">Tickets</label>
                <p className="text-2xl font-bold">{selectedCustomer.totalSupportTickets || 0}</p>
              </div>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}

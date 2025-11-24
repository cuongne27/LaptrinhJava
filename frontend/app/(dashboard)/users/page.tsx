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
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, Check, X, Key } from "lucide-react";

interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roleName: string;
  brandId?: number;
  brandName?: string;
  dealerId?: number;
  dealerName?: string;
  isActive: boolean;
  dateJoined: string;
}

const userSchema = z.object({
  username: z.string().min(1, "Username không được để trống"),
  email: z.string().email("Email không hợp lệ"),
  fullName: z.string().min(1, "Họ tên không được để trống"),
  password: z.string().min(6, "Mật khẩu phải có ít nhất 6 ký tự").optional(),
  roleId: z.number().min(1, "Vui lòng chọn vai trò"),
  brandId: z.number().optional(),
  dealerId: z.number().optional(),
});

type UserForm = z.infer<typeof userSchema>;

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<UserForm>({
    resolver: zodResolver(userSchema),
  });

  useEffect(() => {
    fetchUsers();
  }, [page, search]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<User>>(
        `/users?${params.toString()}`
      );
      setUsers(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching users:", error);
      toast.error("Không thể tải danh sách người dùng");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    reset({
      username: "",
      email: "",
      fullName: "",
      password: "",
      roleId: 0,
    });
    setSelectedUser(null);
    setViewMode("create");
  };

  const handleEdit = (user: User) => {
    reset({
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      roleId: 0, // TODO: Get roleId from user
      brandId: user.brandId,
      dealerId: user.dealerId,
    });
    setSelectedUser(user);
    setViewMode("edit");
  };

  const handleView = (user: User) => {
    setSelectedUser(user);
    setViewMode("detail");
  };

  const handleDelete = async (user: User) => {
    if (!confirm(`Bạn có chắc muốn xóa người dùng "${user.username}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/users/${user.id}`);
      toast.success("Xóa thành công!");
      fetchUsers();
    } catch (error) {
      console.error("Error deleting user:", error);
      toast.error("Không thể xóa người dùng");
    }
  };

  const handleActivate = async (user: User) => {
    try {
      await apiClient.patch(`/users/${user.id}/activate`);
      toast.success("Kích hoạt thành công!");
      fetchUsers();
    } catch (error) {
      console.error("Error activating user:", error);
      toast.error("Không thể kích hoạt người dùng");
    }
  };

  const handleDeactivate = async (user: User) => {
    try {
      await apiClient.patch(`/users/${user.id}/deactivate`);
      toast.success("Vô hiệu hóa thành công!");
      fetchUsers();
    } catch (error) {
      console.error("Error deactivating user:", error);
      toast.error("Không thể vô hiệu hóa người dùng");
    }
  };

  const handleResetPassword = async (user: User) => {
    const newPassword = prompt("Nhập mật khẩu mới:");
    if (!newPassword) return;
    try {
      await apiClient.patch(`/users/${user.id}/reset-password?newPassword=${encodeURIComponent(newPassword)}`);
      toast.success("Reset mật khẩu thành công!");
      fetchUsers();
    } catch (error) {
      console.error("Error resetting password:", error);
      toast.error("Không thể reset mật khẩu");
    }
  };

  const onSubmit = async (data: UserForm) => {
    try {
      if (viewMode === "create") {
        await apiClient.post("/users", data);
        toast.success("Tạo người dùng thành công!");
      } else if (viewMode === "edit" && selectedUser) {
        await apiClient.put(`/users/${selectedUser.id}`, data);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchUsers();
    } catch (error: any) {
      console.error("Error saving user:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu người dùng";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Người dùng</h1>
          <p className="text-muted-foreground">Quản lý nhân viên và người dùng hệ thống</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchUsers} disabled={loading}>
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
              placeholder="Tìm kiếm người dùng..."
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
            {users.map((user) => (
              <Card key={user.id} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{user.fullName}</CardTitle>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(user.isActive ? "ACTIVE" : "INACTIVE")}`}>
                      {user.isActive ? "Hoạt động" : "Ngưng"}
                    </span>
                  </div>
                  <CardDescription>
                    {user.username} - {user.roleName}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Email</span>
                      <span className="font-medium text-sm">{user.email}</span>
                    </div>
                    {user.brandName && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Thương hiệu</span>
                        <span className="font-medium">{user.brandName}</span>
                      </div>
                    )}
                    {user.dealerName && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Đại lý</span>
                        <span className="font-medium">{user.dealerName}</span>
                      </div>
                    )}
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <Button variant="outline" size="sm" onClick={() => handleView(user)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleEdit(user)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    {user.isActive ? (
                      <Button variant="outline" size="sm" onClick={() => handleDeactivate(user)}>
                        <X className="h-4 w-4" />
                      </Button>
                    ) : (
                      <Button variant="outline" size="sm" onClick={() => handleActivate(user)}>
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                    <Button variant="outline" size="sm" onClick={() => handleResetPassword(user)}>
                      <Key className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(user)}
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
        title={viewMode === "create" ? "Thêm người dùng mới" : "Sửa người dùng"}
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
            <label className="text-sm font-medium">Username *</label>
            <Input {...register("username")} className="mt-1" />
            {errors.username && (
              <p className="text-sm text-destructive mt-1">{errors.username.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Email *</label>
            <Input type="email" {...register("email")} className="mt-1" />
            {errors.email && (
              <p className="text-sm text-destructive mt-1">{errors.email.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Họ tên *</label>
            <Input {...register("fullName")} className="mt-1" />
            {errors.fullName && (
              <p className="text-sm text-destructive mt-1">{errors.fullName.message}</p>
            )}
          </div>
          {viewMode === "create" && (
            <div>
              <label className="text-sm font-medium">Mật khẩu *</label>
              <Input type="password" {...register("password")} className="mt-1" />
              {errors.password && (
                <p className="text-sm text-destructive mt-1">{errors.password.message}</p>
              )}
            </div>
          )}
          <div>
            <label className="text-sm font-medium">Vai trò ID *</label>
            <Input
              type="number"
              {...register("roleId", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.roleId && (
              <p className="text-sm text-destructive mt-1">{errors.roleId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Thương hiệu ID</label>
            <Input
              type="number"
              {...register("brandId", { valueAsNumber: true })}
              className="mt-1"
            />
          </div>
          <div>
            <label className="text-sm font-medium">Đại lý ID</label>
            <Input
              type="number"
              {...register("dealerId", { valueAsNumber: true })}
              className="mt-1"
            />
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết người dùng"
        open={viewMode === "detail" && selectedUser !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedUser(null);
        }}
        footer={
          <>
            <Button onClick={() => selectedUser && handleResetPassword(selectedUser)}>
              <Key className="mr-2 h-4 w-4" />
              Reset mật khẩu
            </Button>
            <Button onClick={() => selectedUser && handleEdit(selectedUser)}>
              <Edit className="mr-2 h-4 w-4" />
              Sửa
            </Button>
          </>
        }
      >
        {selectedUser && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Username</label>
              <p className="text-lg font-semibold">{selectedUser.username}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Họ tên</label>
              <p>{selectedUser.fullName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Email</label>
              <p>{selectedUser.email}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Vai trò</label>
              <p>{selectedUser.roleName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedUser.isActive ? "ACTIVE" : "INACTIVE")}`}>
                  {selectedUser.isActive ? "Hoạt động" : "Ngưng hoạt động"}
                </span>
              </p>
            </div>
            {selectedUser.brandName && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Thương hiệu</label>
                <p>{selectedUser.brandName}</p>
              </div>
            )}
            {selectedUser.dealerName && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Đại lý</label>
                <p>{selectedUser.dealerName}</p>
              </div>
            )}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Ngày tham gia</label>
              <p>{formatDate(selectedUser.dateJoined)}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}


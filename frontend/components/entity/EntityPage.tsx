"use client";

import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw } from "lucide-react";
import { formatDate, formatCurrency, getStatusColor } from "@/lib/utils";

interface EntityPageProps<T> {
  title: string;
  description: string;
  data: T[];
  loading: boolean;
  search: string;
  onSearchChange: (value: string) => void;
  onRefresh: () => void;
  onCreate: () => void;
  onView: (item: T) => void;
  onEdit: (item: T) => void;
  onDelete?: (item: T) => void;
  renderItem: (item: T) => React.ReactNode;
  renderCreateForm?: () => React.ReactNode;
  renderEditForm?: (item: T) => React.ReactNode;
  renderDetail?: (item: T) => React.ReactNode;
  pagination?: {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
  };
}

export function EntityPage<T extends { id: number | string }>({
  title,
  description,
  data,
  loading,
  search,
  onSearchChange,
  onRefresh,
  onCreate,
  onView,
  onEdit,
  onDelete,
  renderItem,
  pagination,
}: EntityPageProps<T>) {
  const [selectedItem, setSelectedItem] = useState<T | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{title}</h1>
          <p className="text-muted-foreground">{description}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={onRefresh} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Làm mới
          </Button>
          <Button onClick={onCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Thêm mới
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Tìm kiếm</CardTitle>
          <CardDescription>Tìm kiếm trong danh sách</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm..."
              value={search}
              onChange={(e) => onSearchChange(e.target.value)}
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
              <CardTitle>Danh sách ({data.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {data.map((item) => (
                  <div
                    key={String(item.id)}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                  >
                    {renderItem(item)}
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" onClick={() => onView(item)}>
                        <Eye className="mr-2 h-4 w-4" />
                        Xem
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => onEdit(item)}>
                        <Edit className="mr-2 h-4 w-4" />
                        Sửa
                      </Button>
                      {onDelete && (
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => onDelete(item)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          Xóa
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
                {data.length === 0 && (
                  <div className="text-center py-8 text-muted-foreground">
                    Không có dữ liệu
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {pagination && pagination.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button
                variant="outline"
                onClick={() => pagination.onPageChange(pagination.page - 1)}
                disabled={pagination.page === 0}
              >
                Trước
              </Button>
              <span className="text-sm text-muted-foreground">
                Trang {pagination.page + 1} / {pagination.totalPages}
              </span>
              <Button
                variant="outline"
                onClick={() => pagination.onPageChange(pagination.page + 1)}
                disabled={pagination.page >= pagination.totalPages - 1}
              >
                Sau
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}


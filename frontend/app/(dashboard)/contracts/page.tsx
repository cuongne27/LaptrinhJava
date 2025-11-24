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
import { Search, Plus, Eye, Edit, Trash2, RefreshCw } from "lucide-react";

interface Contract {
  id: number;
  contractNumber: string;
  brandId: number;
  brandName: string;
  dealerId: number;
  dealerName: string;
  startDate: string;
  endDate: string;
  status: string;
  commissionRate: number;
  createdAt: string;
}

const contractSchema = z.object({
  brandId: z.number().min(1, "Vui lòng chọn thương hiệu"),
  dealerId: z.number().min(1, "Vui lòng chọn đại lý"),
  startDate: z.string().min(1, "Vui lòng chọn ngày bắt đầu"),
  endDate: z.string().min(1, "Vui lòng chọn ngày kết thúc"),
  commissionRate: z.number().min(0, "Tỷ lệ hoa hồng phải >= 0").max(100, "Tỷ lệ hoa hồng phải <= 100"),
});

type ContractForm = z.infer<typeof contractSchema>;

export default function ContractsPage() {
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedContract, setSelectedContract] = useState<Contract | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

  const {
    register,
    handleSubmit,
    reset,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<ContractForm>({
    resolver: zodResolver(contractSchema),
  });

  useEffect(() => {
    fetchContracts();
  }, [page]);

  const fetchContracts = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      const response = await apiClient.get<PaginatedResponse<Contract>>(
        `/contracts?${params.toString()}`
      );
      setContracts(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching contracts:", error);
      toast.error("Không thể tải danh sách hợp đồng");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    reset({
      brandId: 0,
      dealerId: 0,
      startDate: "",
      endDate: "",
      commissionRate: 0,
    });
    setSelectedContract(null);
    setViewMode("create");
  };

  const handleEdit = (contract: Contract) => {
    reset({
      brandId: contract.brandId,
      dealerId: contract.dealerId,
      startDate: contract.startDate,
      endDate: contract.endDate,
      commissionRate: contract.commissionRate,
    });
    setSelectedContract(contract);
    setViewMode("edit");
  };

  const handleView = (contract: Contract) => {
    setSelectedContract(contract);
    setViewMode("detail");
  };

  const handleDelete = async (contract: Contract) => {
    if (!confirm(`Bạn có chắc muốn xóa hợp đồng "${contract.contractNumber}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/contracts/${contract.id}`);
      toast.success("Xóa thành công!");
      fetchContracts();
    } catch (error) {
      console.error("Error deleting contract:", error);
      toast.error("Không thể xóa hợp đồng");
    }
  };

  const onSubmit = async (data: ContractForm) => {
    try {
      if (viewMode === "create") {
        toast.success("Tạo hợp đồng thành công!");
      } else if (viewMode === "edit" && selectedContract) {
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      fetchContracts();
    } catch (error: any) {
      console.error("Error saving contract:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu hợp đồng";
      toast.error(errorMessage);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Hợp đồng</h1>
          <p className="text-muted-foreground">Quản lý hợp đồng đại lý</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchContracts} disabled={loading}>
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
            {contracts.map((contract) => (
              <Card key={contract.id} className="overflow-hidden">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{contract.contractNumber}</CardTitle>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(contract.status)}`}>
                      {contract.status}
                    </span>
                  </div>
                  <CardDescription>
                    {formatDate(contract.startDate)} - {formatDate(contract.endDate)}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Thương hiệu</span>
                      <span className="font-medium">{contract.brandName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Đại lý</span>
                      <span className="font-medium">{contract.dealerName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Hoa hồng</span>
                      <span className="font-medium">{contract.commissionRate}%</span>
                    </div>
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <Button variant="outline" size="sm" onClick={() => handleView(contract)}>
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleEdit(contract)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(contract)}
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
        title={viewMode === "create" ? "Thêm hợp đồng mới" : "Sửa hợp đồng"}
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
          <div>
            <label className="text-sm font-medium">Thương hiệu ID *</label>
            <Input
              type="number"
              {...register("brandId", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.brandId && (
              <p className="text-sm text-destructive mt-1">{errors.brandId.message}</p>
            )}
          </div>
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
                <label className="text-sm font-medium">Ngày bắt đầu *</label>
                <Input type="date" {...register("startDate")} className="mt-1" />
                {errors.startDate && (
                  <p className="text-sm text-destructive mt-1">{errors.startDate.message}</p>
                )}
              </div>
              <div>
                <label className="text-sm font-medium">Ngày kết thúc *</label>
                <Input type="date" {...register("endDate")} className="mt-1" />
                {errors.endDate && (
                  <p className="text-sm text-destructive mt-1">{errors.endDate.message}</p>
                )}
              </div>
              <div>
                <label className="text-sm font-medium">Tỷ lệ hoa hồng (%) *</label>
                <Input
                  type="number"
                  step="0.01"
                  {...register("commissionRate", { valueAsNumber: true })}
                  className="mt-1"
                />
                {errors.commissionRate && (
                  <p className="text-sm text-destructive mt-1">{errors.commissionRate.message}</p>
                )}
        </div>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết hợp đồng"
        open={viewMode === "detail" && selectedContract !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedContract(null);
        }}
        footer={
          <Button onClick={() => selectedContract && handleEdit(selectedContract)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedContract && (
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Số hợp đồng</label>
              <p className="text-lg font-semibold">{selectedContract.contractNumber}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedContract.status)}`}>
                  {selectedContract.status}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Thương hiệu</label>
              <p>{selectedContract.brandName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Đại lý</label>
              <p>{selectedContract.dealerName}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Thời gian</label>
              <p>{formatDate(selectedContract.startDate)} - {formatDate(selectedContract.endDate)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tỷ lệ hoa hồng</label>
              <p>{selectedContract.commissionRate}%</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}
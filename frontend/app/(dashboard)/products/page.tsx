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
import { formatCurrency } from "@/lib/utils";
import type { Product, PaginatedResponse, Brand } from "@/types";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw, X } from "lucide-react";
import Image from "next/image";

const productSchema = z.object({
  productName: z.string().min(1, "Tên sản phẩm không được để trống"),
  version: z.string().optional(),
  msrp: z.number().min(0.01, "Giá phải lớn hơn 0"),
  description: z.string().optional(),
  imageUrl: z
    .string()
    .optional()
    .refine(
      (val) => !val || val === "" || val.startsWith("http") || val.startsWith("/"),
      "URL hình ảnh không hợp lệ"
    ),
  videoUrl: z
    .string()
    .optional()
    .refine(
      (val) => !val || val === "" || val.startsWith("http"),
      "URL video phải là full URL (bắt đầu bằng http)"
    ),
  brandId: z.number().min(1, "Vui lòng chọn thương hiệu"),
  isActive: z.boolean().optional(),
});

type ProductForm = z.infer<typeof productSchema>;

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");
  const [variants, setVariants] = useState<Array<{ color: string; colorCode: string; availableQuantity: number }>>([
    { color: "", colorCode: "#FFFFFF", availableQuantity: 0 },
  ]);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    trigger,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<ProductForm>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      isActive: true,
    },
  });

  useEffect(() => {
    fetchProducts();
    fetchBrands();
  }, [page, search]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        size: "20",
      });
      if (search) {
        params.append("searchKeyword", search);
      }
      const response = await apiClient.get<PaginatedResponse<Product>>(
        `/products?${params.toString()}`
      );
      setProducts(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching products:", error);
      toast.error("Không thể tải danh sách sản phẩm");
    } finally {
      setLoading(false);
    }
  };

  const fetchBrands = async () => {
    try {
      const response = await apiClient.get<Brand[]>("/brands/all");
      setBrands(response.data);
    } catch (error) {
      console.error("Error fetching brands:", error);
    }
  };

  const handleCreate = () => {
    reset({
      productName: "",
      version: "",
      msrp: 0,
      description: "",
      imageUrl: "",
      videoUrl: "",
      brandId: brands[0]?.id || 0,
      isActive: true,
    });
    setVariants([{ color: "", colorCode: "#FFFFFF", availableQuantity: 0 }]);
    setSelectedProduct(null);
    setViewMode("create");
  };

  const handleEdit = (product: Product) => {
    reset({
      productName: product.productName,
      version: product.version || "",
      msrp: Number(product.msrp),
      description: "",
      imageUrl: product.imageUrl || "",
      videoUrl: "",
      brandId: product.brandId,
      isActive: product.isActive,
    });
    setVariants([{ color: "", colorCode: "#FFFFFF", availableQuantity: 0 }]);
    setSelectedProduct(product);
    setViewMode("edit");
  };

  const handleView = (product: Product) => {
    setSelectedProduct(product);
    setViewMode("detail");
  };

  const handleDelete = async (product: Product) => {
    if (!confirm(`Bạn có chắc muốn xóa sản phẩm "${product.productName}"?`)) {
      return;
    }
    try {
      await apiClient.delete(`/products/${product.id}`);
      toast.success("Xóa thành công!");
      fetchProducts();
    } catch (error) {
      console.error("Error deleting product:", error);
      toast.error("Không thể xóa sản phẩm");
    }
  };

  const handleDeletePermanent = async (product: Product) => {
    if (
      !confirm(
        `Bạn có chắc muốn XÓA VĨNH VIỄN sản phẩm "${product.productName}"?\n\nHành động này không thể hoàn tác!`
      )
    ) {
      return;
    }
    try {
      await apiClient.delete(`/products/${product.id}/permanent`);
      toast.success("Xóa vĩnh viễn thành công!");
      fetchProducts();
    } catch (error: any) {
      console.error("Error permanently deleting product:", error);
      toast.error(error.response?.data?.message || "Không thể xóa vĩnh viễn sản phẩm");
    }
  };

  const onSubmit = async (data: ProductForm) => {
    console.log("=== FORM SUBMIT ===");
    console.log("Form data:", data);
    console.log("Variants:", variants);
    console.log("Form errors:", errors);
    
    try {
      // Filter out empty variants
      const validVariants = variants.filter((v) => v.color.trim() !== "");

      if (validVariants.length === 0) {
        toast.error("Phải có ít nhất 1 màu sắc");
        return;
      }

      // Normalize color codes (convert 3-digit to 6-digit hex)
      const normalizeColorCode = (code: string): string | null => {
        if (!code || code.trim() === "") return null;
        // Remove # if present
        let hex = code.replace("#", "");
        // Convert 3-digit to 6-digit
        if (hex.length === 3) {
          hex = hex.split("").map((c) => c + c).join("");
        }
        // Validate it's a valid hex
        if (/^[0-9A-Fa-f]{6}$/.test(hex)) {
          return `#${hex.toUpperCase()}`;
        }
        return null;
      };

      const submitData = {
        productName: data.productName,
        version: data.version || null,
        msrp: data.msrp,
        description: data.description || null,
        imageUrl: data.imageUrl || null,
        videoUrl: data.videoUrl || null,
        brandId: data.brandId,
        isActive: data.isActive !== undefined ? data.isActive : true,
        technicalSpecs: null, // TechnicalSpecs is optional, can be null
        features: null, // Features is optional, can be null
        variants: validVariants.map((v) => ({
          color: v.color.trim(),
          colorCode: normalizeColorCode(v.colorCode),
          availableQuantity: v.availableQuantity || 0,
        })),
      };

      if (viewMode === "create") {
        await apiClient.post("/products/create", submitData);
        toast.success("Tạo sản phẩm thành công!");
      } else if (viewMode === "edit" && selectedProduct) {
        await apiClient.put(`/products/${selectedProduct.id}`, submitData);
        toast.success("Cập nhật thành công!");
      }
      setViewMode("list");
      reset();
      setVariants([{ color: "", colorCode: "#FFFFFF", availableQuantity: 0 }]);
      fetchProducts();
    } catch (error: any) {
      console.error("Error saving product:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Không thể lưu sản phẩm";
      toast.error(errorMessage);
    }
  };

  const addVariant = () => {
    setVariants([...variants, { color: "", colorCode: "#FFFFFF", availableQuantity: 0 }]);
  };

  const removeVariant = (index: number) => {
    if (variants.length > 1) {
      setVariants(variants.filter((_, i) => i !== index));
    }
  };

  const updateVariant = (index: number, field: string, value: any) => {
    const updated = [...variants];
    updated[index] = { ...updated[index], [field]: value };
    setVariants(updated);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Sản phẩm</h1>
          <p className="text-muted-foreground">Quản lý danh mục sản phẩm xe điện</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchProducts} disabled={loading}>
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
              placeholder="Tìm kiếm sản phẩm..."
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
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {products.map((product) => (
              <Card key={product.id} className="overflow-hidden">
                {product.imageUrl && (
                  <div className="relative h-48 w-full bg-muted">
                    <Image
                      src={product.imageUrl}
                      alt={product.productName}
                      fill
                      className="object-cover"
                    />
                  </div>
                )}
                <CardHeader>
                  <CardTitle className="text-lg">{product.productName}</CardTitle>
                  <CardDescription>{product.version}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Thương hiệu</span>
                      <span className="font-medium">{product.brandName}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Giá</span>
                      <span className="font-bold text-primary">
                        {formatCurrency(product.msrp)}
                      </span>
                    </div>
                    {product.availableQuantity !== undefined && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Tồn kho</span>
                        <span className="font-medium">{product.availableQuantity} đơn vị</span>
                      </div>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => handleView(product)} className="flex-1">
                      <Eye className="mr-2 h-4 w-4" />
                      Xem
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleEdit(product)} className="flex-1">
                      <Edit className="mr-2 h-4 w-4" />
                      Sửa
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handleDelete(product)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {products.length === 0 && (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                Không có dữ liệu
              </CardContent>
            </Card>
          )}

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
        title={viewMode === "create" ? "Thêm sản phẩm mới" : "Sửa sản phẩm"}
        open={viewMode === "create" || viewMode === "edit"}
        onClose={() => {
          setViewMode("list");
          reset();
        }}
        footer={
          <>
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setViewMode("list");
                reset();
                setVariants([{ color: "", colorCode: "#FFFFFF", availableQuantity: 0 }]);
              }}
            >
              Hủy
            </Button>
            <Button 
              type="button"
              onClick={async () => {
                console.log("Button clicked, triggering form validation");
                
                // Validate form fields
                const isValid = await trigger();
                console.log("Form validation result:", isValid);
                console.log("Form errors:", errors);
                
                // Validate variants separately
                const validVariants = variants.filter((v) => v.color.trim() !== "");
                if (validVariants.length === 0) {
                  toast.error("Phải có ít nhất 1 màu sắc");
                  return;
                }
                
                if (isValid) {
                  const formData = getValues();
                  console.log("Form values:", formData);
                  console.log("Valid variants:", validVariants);
                  onSubmit(formData);
                } else {
                  console.log("Validation failed, errors:", errors);
                  // Show first error message
                  const firstError = Object.values(errors)[0];
                  if (firstError) {
                    toast.error(firstError.message as string);
                  } else {
                    toast.error("Vui lòng điền đầy đủ thông tin bắt buộc");
                  }
                }
              }}
              disabled={isSubmitting}
            >
              {isSubmitting ? "Đang lưu..." : viewMode === "create" ? "Tạo" : "Cập nhật"}
            </Button>
          </>
        }
      >
        <form id="product-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Tên sản phẩm *</label>
            <Input {...register("productName")} className="mt-1" />
            {errors.productName && (
              <p className="text-sm text-destructive mt-1">{errors.productName.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Phiên bản</label>
            <Input {...register("version")} className="mt-1" />
          </div>
          <div>
            <label className="text-sm font-medium">Giá bán lẻ đề xuất (VND) *</label>
            <Input
              type="number"
              step="0.01"
              {...register("msrp", { valueAsNumber: true })}
              className="mt-1"
            />
            {errors.msrp && (
              <p className="text-sm text-destructive mt-1">{errors.msrp.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Thương hiệu *</label>
            <select
              {...register("brandId", { valueAsNumber: true })}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            >
              <option value="">Chọn thương hiệu</option>
              {brands.map((brand) => (
                <option key={brand.id} value={brand.id}>
                  {brand.brandName}
                </option>
              ))}
            </select>
            {errors.brandId && (
              <p className="text-sm text-destructive mt-1">{errors.brandId.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">Mô tả</label>
            <textarea
              {...register("description")}
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm mt-1"
            />
          </div>
          <div>
            <label className="text-sm font-medium">URL hình ảnh</label>
            <Input {...register("imageUrl")} className="mt-1" placeholder="/images/... hoặc https://..." />
            {errors.imageUrl && (
              <p className="text-sm text-destructive mt-1">{errors.imageUrl.message}</p>
            )}
          </div>
          <div>
            <label className="text-sm font-medium">URL video</label>
            <Input {...register("videoUrl")} className="mt-1" placeholder="https://..." />
            {errors.videoUrl && (
              <p className="text-sm text-destructive mt-1">{errors.videoUrl.message}</p>
            )}
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              {...register("isActive")}
              className="h-4 w-4"
            />
            <label className="text-sm font-medium">Kích hoạt</label>
          </div>

          <div className="border-t pt-4">
            <div className="flex items-center justify-between mb-3">
              <label className="text-sm font-medium">Màu sắc *</label>
              <Button type="button" variant="outline" size="sm" onClick={addVariant}>
                <Plus className="h-4 w-4 mr-1" />
                Thêm màu
              </Button>
            </div>
            <div className="space-y-3">
              {variants.map((variant, index) => (
                <div key={index} className="flex gap-2 items-start p-3 border rounded-lg">
                  <div className="flex-1 grid grid-cols-3 gap-2">
                    <div>
                      <label className="text-xs text-muted-foreground">Tên màu *</label>
                      <Input
                        value={variant.color}
                        onChange={(e) => updateVariant(index, "color", e.target.value)}
                        placeholder="VD: Đỏ, Xanh..."
                        className="mt-1"
                      />
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground">Mã màu</label>
                      <div className="flex gap-1 mt-1">
                        <Input
                          type="color"
                          value={variant.colorCode}
                          onChange={(e) => updateVariant(index, "colorCode", e.target.value)}
                          className="h-10 w-16 p-1"
                        />
                        <Input
                          value={variant.colorCode}
                          onChange={(e) => updateVariant(index, "colorCode", e.target.value)}
                          placeholder="#FFFFFF"
                          className="flex-1"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground">Số lượng</label>
                      <Input
                        type="number"
                        value={variant.availableQuantity}
                        onChange={(e) =>
                          updateVariant(index, "availableQuantity", parseInt(e.target.value) || 0)
                        }
                        className="mt-1"
                        min="0"
                      />
                    </div>
                  </div>
                  {variants.length > 1 && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => removeVariant(index)}
                      className="mt-6"
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
            {variants.filter((v) => v.color.trim() === "").length > 0 && (
              <p className="text-sm text-destructive mt-2">
                Vui lòng điền đầy đủ tên màu hoặc xóa các màu trống
              </p>
            )}
          </div>
        </form>
      </EntityModal>

      {/* Detail Modal */}
      <EntityModal
        title="Chi tiết sản phẩm"
        open={viewMode === "detail" && selectedProduct !== null}
        onClose={() => {
          setViewMode("list");
          setSelectedProduct(null);
        }}
        footer={
          <Button onClick={() => selectedProduct && handleEdit(selectedProduct)}>
            <Edit className="mr-2 h-4 w-4" />
            Sửa
          </Button>
        }
      >
        {selectedProduct && (
          <div className="space-y-4">
            {selectedProduct.imageUrl && (
              <div className="relative h-64 w-full bg-muted rounded-lg overflow-hidden">
                <Image
                  src={selectedProduct.imageUrl}
                  alt={selectedProduct.productName}
                  fill
                  className="object-cover"
                />
              </div>
            )}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Tên sản phẩm</label>
              <p className="text-lg font-semibold">{selectedProduct.productName}</p>
            </div>
            {selectedProduct.version && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Phiên bản</label>
                <p>{selectedProduct.version}</p>
              </div>
            )}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Giá</label>
              <p className="text-2xl font-bold text-primary">
                {formatCurrency(selectedProduct.msrp)}
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Thương hiệu</label>
              <p>{selectedProduct.brandName}</p>
            </div>
            {selectedProduct.availableQuantity !== undefined && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Tồn kho</label>
                <p>{selectedProduct.availableQuantity} đơn vị</p>
              </div>
            )}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Trạng thái</label>
              <p>{selectedProduct.isActive ? "Đang hoạt động" : "Ngưng hoạt động"}</p>
            </div>
          </div>
        )}
      </EntityModal>
    </div>
  );
}

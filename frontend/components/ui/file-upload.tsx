// components/ui/file-upload.tsx
"use client";

import { useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { X, ImageIcon } from "lucide-react";
import Image from "next/image";
import { apiClient } from "@/lib/api/client";
import toast from "react-hot-toast";

interface FileUploadProps {
  value?: string;
  onChange: (url: string) => void;
  onRemove: () => void;
  accept?: string;
}

export function FileUpload({ value, onChange, onRemove, accept = "image/*" }: FileUploadProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      toast.error("File quá lớn. Tối đa 5MB");
      return;
    }

    if (!file.type.startsWith("image/")) {
      toast.error("Chỉ cho phép upload file ảnh");
      return;
    }

    try {
      setUploading(true);
      
      const formData = new FormData();
      formData.append("file", file);

      const response = await apiClient.post<{
        success: boolean;
        url: string;
        message: string;
      }>("/files/upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      if (response.data.success) {
        onChange(response.data.url);
        toast.success("Upload thành công!");
      } else {
        toast.error(response.data.message || "Upload thất bại");
      }
    } catch (error: any) {
      console.error("Upload error:", error);
      toast.error(error.response?.data?.message || "Lỗi khi upload file");
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  // ✅ Hàm tạo full image URL
  const getImageUrl = (url: string) => {
    if (!url) return "";
    if (url.startsWith("http")) return url;
    return `http://localhost:8080${url}`;
  };

  return (
    <div className="space-y-2">
      <input
        ref={fileInputRef}
        type="file"
        accept={accept}
        onChange={handleFileSelect}
        className="hidden"
      />

      {value ? (
        <div className="relative group">
          <div className="relative h-48 w-full rounded-lg overflow-hidden border">
            <Image
              src={getImageUrl(value)} // ✅ SỬ DỤNG HELPER
              alt="Preview"
              fill
              className="object-cover"
              unoptimized // ✅ BẮT BUỘC với Next.js external images
            />
          </div>
          <Button
            type="button"
            variant="destructive"
            size="icon"
            className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
            onClick={onRemove}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : (
        <div
          onClick={() => fileInputRef.current?.click()}
          className="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer hover:border-primary transition-colors"
        >
          {uploading ? (
            <div className="flex flex-col items-center gap-2">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
              <p className="text-sm text-muted-foreground">Đang upload...</p>
            </div>
          ) : (
            <div className="flex flex-col items-center gap-2">
              <ImageIcon className="h-8 w-8 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Click để upload ảnh</p>
                <p className="text-xs text-muted-foreground">PNG, JPG, JPEG (tối đa 5MB)</p>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
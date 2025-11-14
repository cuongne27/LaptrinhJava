"use client";

import { useState, useEffect } from "react";
import type { FilterOption } from "../filter/FilterDropdown";

interface CrudField {
  name: string;
  label: string;
  type: "text" | "number" | "date" | "select" | "textarea";
  required?: boolean;
  options?: FilterOption[];
}

interface CrudFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  mode: "create" | "edit" | "delete";
  title: string;
  fields: CrudField[];
  initialData?: any;
  onSubmit: (data: any) => Promise<void>;
  isSubmitting: boolean;
}

export default function CrudFormModal({
  isOpen,
  onClose,
  mode,
  title,
  fields,
  initialData,
  onSubmit,
  isSubmitting,
}: CrudFormModalProps) {
  const [formData, setFormData] = useState<Record<string, any>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (initialData && mode === "edit") {
      const data: Record<string, any> = {};
      fields.forEach(field => {
        data[field.name] = initialData[field.name as keyof typeof initialData] || "";
      });
      setFormData(data);
    } else {
      setFormData({});
    }
    setErrors({});
  }, [initialData, mode, fields]);

  const handleInputChange = (name: string, value: any) => {
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: "" }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    
    fields.forEach(field => {
      if (mode === "create" && field.required && !formData[field.name]) {
        newErrors[field.name] = `${field.label} là bắt buộc`;
      }
      
      if (field.type === "number" && formData[field.name] && isNaN(Number(formData[field.name]))) {
        newErrors[field.name] = `${field.label} phải là số hợp lệ`;
      }
      
      if (field.type === "date" && formData[field.name]) {
        const date = new Date(formData[field.name]);
        if (isNaN(date.getTime())) {
          newErrors[field.name] = `${field.label} phải là ngày hợp lệ`;
        }
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    const submitData = { ...formData };
    
    // Convert number fields
    fields.forEach(field => {
      if (field.type === "number" && submitData[field.name]) {
        submitData[field.name] = Number(submitData[field.name]);
      }
    });

    await onSubmit(submitData);
  };

  const renderField = (field: CrudField) => {
    const error = errors[field.name];
    const value = formData[field.name] || "";

    const baseInputClasses = `
      w-full px-3 py-2 rounded-lg border
      transition-colors duration-200
      focus:outline-none focus:ring-2 focus:ring-rose-500
      ${error ? "border-red-300 bg-red-50" : "border-gray-300 bg-white"}
      ${mode === "delete" ? "bg-gray-100 cursor-not-allowed" : ""}
    `;

    switch (field.type) {
      case "select":
        return (
          <div key={field.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {field.label} {field.required && <span className="text-red-500">*</span>}
            </label>
            <select
              value={value}
              onChange={(e) => handleInputChange(field.name, e.target.value)}
              disabled={mode === "delete"}
              className={baseInputClasses}
            >
              <option value="">Chọn...</option>
              {field.options?.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {error && <p className="text-sm text-red-600">{error}</p>}
          </div>
        );

      case "textarea":
        return (
          <div key={field.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {field.label} {field.required && <span className="text-red-500">*</span>}
            </label>
            <textarea
              value={value}
              onChange={(e) => handleInputChange(field.name, e.target.value)}
              disabled={mode === "delete"}
              rows={4}
              className={baseInputClasses}
            />
            {error && <p className="text-sm text-red-600">{error}</p>}
          </div>
        );

      default:
        return (
          <div key={field.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {field.label} {field.required && <span className="text-red-500">*</span>}
            </label>
            <input
              type={field.type}
              value={value}
              onChange={(e) => handleInputChange(field.name, e.target.value)}
              disabled={mode === "delete"}
              className={baseInputClasses}
            />
            {error && <p className="text-sm text-red-600">{error}</p>}
          </div>
        );
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-screen items-center justify-center p-4">
        {/* Backdrop */}
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
          onClick={onClose}
        />

        {/* Modal */}
        <div className="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
          {/* Header */}
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-900">{title}</h2>
            {mode === "delete" && initialData && (
              <p className="mt-2 text-sm text-gray-600">
                Bạn có chắc chắn muốn xóa hợp đồng này? Hành động này không thể hoàn tác.
              </p>
            )}
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {mode !== "delete" ? (
              fields.map(renderField)
            ) : (
              <div className="space-y-2">
                <p className="text-sm text-gray-600">
                  <strong>ID:</strong> {initialData?.id}
                </p>
                <p className="text-sm text-gray-600">
                  <strong>Thương hiệu:</strong> {initialData?.brandName}
                </p>
                <p className="text-sm text-gray-600">
                  <strong>Đại lý:</strong> {initialData?.dealerName}
                </p>
              </div>
            )}

            {/* Actions */}
            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                disabled={isSubmitting}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors disabled:opacity-50"
              >
                Hủy
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className={`
                  px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50
                  ${mode === "delete" 
                    ? "bg-red-600 hover:bg-red-700" 
                    : "bg-rose-600 hover:bg-rose-700"
                  }
                `}
              >
                {isSubmitting ? "Đang xử lý..." : (
                  mode === "create" ? "Thêm" :
                  mode === "edit" ? "Cập nhật" : "Xóa"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

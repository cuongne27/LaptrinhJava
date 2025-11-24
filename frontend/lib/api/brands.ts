import { apiClient } from "./client";
import type { Brand, PaginatedResponse } from "@/types";

export const brandsApi = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    direction?: string;
  }): Promise<PaginatedResponse<Brand>> => {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined) queryParams.append("page", params.page.toString());
    if (params?.size !== undefined) queryParams.append("size", params.size.toString());
    if (params?.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params?.direction) queryParams.append("direction", params.direction);

    const response = await apiClient.get(`/brands?${queryParams.toString()}`);
    return response.data;
  },

  getAllWithoutPagination: async (): Promise<Brand[]> => {
    const response = await apiClient.get("/brands/all");
    return response.data;
  },

  getById: async (id: number): Promise<Brand> => {
    const response = await apiClient.get(`/brands/${id}`);
    return response.data;
  },

  create: async (data: {
    brandName: string;
    headquartersAddress?: string;
    taxCode?: string;
    contactInfo?: string;
  }): Promise<Brand> => {
    const response = await apiClient.post("/brands/create", data);
    return response.data;
  },

  update: async (id: number, data: {
    brandName: string;
    headquartersAddress?: string;
    taxCode?: string;
    contactInfo?: string;
  }): Promise<Brand> => {
    const response = await apiClient.put(`/brands/update/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/brands/delete/${id}`);
  },
};


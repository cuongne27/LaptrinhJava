import axios, { AxiosError, AxiosInstance } from "axios";
import toast from "react-hot-toast";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: `${API_URL}/api`,
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Request interceptor - Add auth token
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("token");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor - Handle errors
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401) {
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          window.location.href = "/login";
          toast.error("Phiên đăng nhập đã hết hạn");
        } else if (error.response?.status === 403) {
          toast.error("Bạn không có quyền thực hiện thao tác này");
        } else if (error.response?.status === 404) {
          toast.error("Không tìm thấy dữ liệu");
        } else if (error.response?.status >= 500) {
          toast.error("Lỗi server. Vui lòng thử lại sau");
        } else if (error.response?.data) {
          const message = (error.response.data as any).message || "Đã xảy ra lỗi";
          toast.error(message);
        }
        return Promise.reject(error);
      }
    );
  }

  get instance() {
    return this.client;
  }
}

export const apiClient = new ApiClient().instance;


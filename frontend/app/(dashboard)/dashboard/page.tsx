"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { apiClient } from "@/lib/api/client";
import { formatCurrency, formatDate } from "@/lib/utils";
import {
  DollarSign,
  ShoppingCart,
  TrendingUp,
  AlertTriangle,
  CreditCard,
  Package,
  Users,
  Calendar,
  Building2,
  Zap,
} from "lucide-react";
import type { DashboardSummary } from "@/types";
import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function DashboardPage() {
  const [data, setData] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalBrands: 0,
    totalDealers: 0,
    totalProducts: 0,
    totalCustomers: 0,
    totalOrders: 0,
    totalVehicles: 0,
    totalAppointments: 0,
    totalTickets: 0,
  });

  useEffect(() => {
    fetchDashboardData();
    fetchStats();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const response = await apiClient.get("/reports/dashboard");
      setData(response.data);
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      // Fetch counts for each entity
      // Try /products first (for ADMIN/BRAND_MANAGER), fallback to /dealer/products (for DEALER_STAFF)
      const productsPromise = apiClient.get("/products?size=1").catch(() => 
        apiClient.get("/dealer/products?size=1").catch(() => ({ data: { totalElements: 0 } }))
      );

      const [brands, dealers, products, customers, orders, vehicles, appointments, tickets] =
        await Promise.all([
          apiClient.get("/brands?size=1").catch(() => ({ data: { totalElements: 0 } })),
          apiClient.get("/dealers/filter?size=1").catch(() => ({ data: { totalElements: 0 } })),
          productsPromise,
          apiClient.get("/customers?size=1").catch(() => ({ data: { totalElements: 0 } })),
          apiClient.get("/sales-orders?size=1").catch(() => ({ data: { totalElements: 0 } })),
          apiClient.get("/vehicles?size=1").catch(() => ({ data: { totalElements: 0 } })),
          apiClient.get("/appointments?size=1").catch(() => ({ data: { totalElements: 0 } })),
          apiClient.get("/support-tickets?size=1").catch(() => ({ data: { totalElements: 0 } })),
        ]);

      setStats({
        totalBrands: brands.data.totalElements || 0,
        totalDealers: dealers.data.totalElements || 0,
        totalProducts: products.data.totalElements || 0,
        totalCustomers: customers.data.totalElements || 0,
        totalOrders: orders.data.totalElements || 0,
        totalVehicles: vehicles.data.totalElements || 0,
        totalAppointments: appointments.data.totalElements || 0,
        totalTickets: tickets.data.totalElements || 0,
      });
    } catch (error) {
      console.error("Error fetching stats:", error);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-muted-foreground">Đang tải...</div>
      </div>
    );
  }

  const mainStats = data
    ? [
        {
          title: "Doanh thu",
          value: formatCurrency(data.totalRevenue),
          change: `${data.growthRate > 0 ? "+" : ""}${data.growthRate.toFixed(1)}%`,
          icon: DollarSign,
          trend: data.growthRate > 0 ? "up" : "down",
        },
        {
          title: "Tổng đơn hàng",
          value: data.totalOrders.toLocaleString(),
          change: "Tháng này",
          icon: ShoppingCart,
        },
        {
          title: "Tồn kho thấp",
          value: data.lowStockCount.toString(),
          change: `${data.outOfStockCount} hết hàng`,
          icon: AlertTriangle,
          variant: data.lowStockCount > 0 ? "destructive" : "default",
        },
        {
          title: "Chờ thanh toán",
          value: formatCurrency(data.totalPendingPayment),
          change: "Chưa thanh toán",
          icon: CreditCard,
        },
      ]
    : [];

  const entityStats = [
    { label: "Thương hiệu", value: stats.totalBrands, icon: Zap, href: "/brands" },
    { label: "Đại lý", value: stats.totalDealers, icon: Building2, href: "/dealers" },
    { label: "Sản phẩm", value: stats.totalProducts, icon: Package, href: "/products" },
    { label: "Khách hàng", value: stats.totalCustomers, icon: Users, href: "/customers" },
    { label: "Đơn hàng", value: stats.totalOrders, icon: ShoppingCart, href: "/orders" },
    { label: "Xe", value: stats.totalVehicles, icon: Package, href: "/vehicles" },
    { label: "Lịch hẹn", value: stats.totalAppointments, icon: Calendar, href: "/appointments" },
    { label: "Tickets", value: stats.totalTickets, icon: AlertTriangle, href: "/support-tickets" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">
          Tổng quan hệ thống - {data ? formatDate(data.currentDate) : formatDate(new Date())}
        </p>
      </div>

      {/* Main Statistics */}
      {data && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {mainStats.map((stat) => {
            const Icon = stat.icon;
            return (
              <Card key={stat.title}>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
                  <Icon className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{stat.value}</div>
                  <p className="text-xs text-muted-foreground mt-1">{stat.change}</p>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Entity Statistics */}
      <Card>
        <CardHeader>
          <CardTitle>Tổng quan hệ thống</CardTitle>
          <CardDescription>Số lượng các entity trong hệ thống</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {entityStats.map((stat) => {
              const Icon = stat.icon;
              return (
                <Link key={stat.label} href={stat.href}>
                  <Card className="hover:bg-accent transition-colors cursor-pointer">
                    <CardContent className="p-6">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            {stat.label}
                          </p>
                          <p className="text-2xl font-bold mt-1">{stat.value}</p>
                        </div>
                        <Icon className="h-8 w-8 text-muted-foreground" />
                      </div>
                    </CardContent>
                  </Card>
                </Link>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Top Products & Alerts */}
      {data && (
        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Sản phẩm bán chạy</CardTitle>
              <CardDescription>Top sản phẩm trong tháng</CardDescription>
            </CardHeader>
            <CardContent>
              {data.topProducts && data.topProducts.length > 0 ? (
                <div className="space-y-4">
                  {data.topProducts.slice(0, 5).map((product, index) => (
                    <div
                      key={product.productId}
                      className="flex items-center justify-between"
                    >
                      <div className="flex items-center gap-3">
                        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-sm font-medium">
                          {index + 1}
                        </div>
                        <div>
                          <p className="font-medium">{product.productName}</p>
                          <p className="text-sm text-muted-foreground">
                            {product.unitsSold} đơn vị
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-medium">{formatCurrency(product.revenue)}</p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Chưa có dữ liệu</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Cảnh báo tồn kho</CardTitle>
              <CardDescription>Các sản phẩm cần chú ý</CardDescription>
            </CardHeader>
            <CardContent>
              {data.recentAlerts && data.recentAlerts.length > 0 ? (
                <div className="space-y-3">
                  {data.recentAlerts.slice(0, 5).map((alert, index) => (
                    <div
                      key={index}
                      className="flex items-start gap-3 p-3 rounded-lg bg-yellow-50 border border-yellow-200"
                    >
                      <AlertTriangle className="h-5 w-5 text-yellow-600 mt-0.5" />
                      <div className="flex-1">
                        <p className="font-medium text-sm">{alert.productName}</p>
                        <p className="text-xs text-muted-foreground">{alert.message}</p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Không có cảnh báo</p>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Thao tác nhanh</CardTitle>
          <CardDescription>Các thao tác thường dùng</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-2">
            <Link href="/products">
              <Button variant="outline">Quản lý sản phẩm</Button>
            </Link>
            <Link href="/orders">
              <Button variant="outline">Xem đơn hàng</Button>
            </Link>
            <Link href="/customers">
              <Button variant="outline">Quản lý khách hàng</Button>
            </Link>
            <Link href="/inventory">
              <Button variant="outline">Kiểm tra tồn kho</Button>
            </Link>
            <Link href="/appointments">
              <Button variant="outline">Lịch hẹn</Button>
            </Link>
            <Link href="/reports">
              <Button variant="outline">Báo cáo</Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

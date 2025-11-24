"use client";

import { useState, useEffect } from "react";
import { apiClient } from "@/lib/api/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import toast from "react-hot-toast";
import { formatCurrency, formatDate } from "@/lib/utils";
import { Download, RefreshCw, BarChart3, TrendingUp, Package, DollarSign } from "lucide-react";

interface SalesReport {
  totalRevenue: number;
  totalOrders: number;
  growthRate: number;
  topProducts: Array<{
    productId: number;
    productName: string;
    unitsSold: number;
    revenue: number;
  }>;
}

interface InventoryReport {
  totalProducts: number;
  lowStockCount: number;
  outOfStockCount: number;
  alerts: Array<{
    productId: number;
    productName: string;
    alertType: string;
    message: string;
  }>;
}

interface RevenueReport {
  totalRevenue: number;
  totalPending: number;
  totalCompleted: number;
}

export default function ReportsPage() {
  const [salesReport, setSalesReport] = useState<SalesReport | null>(null);
  const [inventoryReport, setInventoryReport] = useState<InventoryReport | null>(null);
  const [revenueReport, setRevenueReport] = useState<RevenueReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (startDate) params.append("startDate", startDate);
      if (endDate) params.append("endDate", endDate);

      const [salesRes, inventoryRes, revenueRes] = await Promise.all([
        apiClient.get<SalesReport>(`/reports/sales?${params.toString()}`),
        apiClient.get<InventoryReport>(`/reports/inventory?${params.toString()}`),
        apiClient.get<RevenueReport>(`/reports/revenue?${params.toString()}`),
      ]);

      setSalesReport(salesRes.data);
      setInventoryReport(inventoryRes.data);
      setRevenueReport(revenueRes.data);
    } catch (error) {
      console.error("Error fetching reports:", error);
      toast.error("Không thể tải báo cáo");
    } finally {
      setLoading(false);
    }
  };

  const handleExportSales = async () => {
    try {
      const params = new URLSearchParams();
      if (startDate) params.append("startDate", startDate);
      if (endDate) params.append("endDate", endDate);
      params.append("format", "EXCEL");

      const response = await apiClient.get(`/reports/sales/export?${params.toString()}`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `sales-report-${new Date().toISOString().split("T")[0]}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success("Xuất báo cáo thành công!");
    } catch (error) {
      console.error("Error exporting sales report:", error);
      toast.error("Không thể xuất báo cáo");
    }
  };

  const handleExportInventory = async () => {
    try {
      const params = new URLSearchParams();
      if (startDate) params.append("startDate", startDate);
      if (endDate) params.append("endDate", endDate);

      const response = await apiClient.get(`/reports/inventory/export?${params.toString()}`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `inventory-report-${new Date().toISOString().split("T")[0]}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success("Xuất báo cáo thành công!");
    } catch (error) {
      console.error("Error exporting inventory report:", error);
      toast.error("Không thể xuất báo cáo");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Báo cáo</h1>
          <p className="text-muted-foreground">Xem và xuất các báo cáo hệ thống</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={fetchReports} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Làm mới
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Lọc theo thời gian</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4">
            <div className="flex-1">
              <label className="text-sm font-medium">Từ ngày</label>
              <Input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="mt-1"
              />
            </div>
            <div className="flex-1">
              <label className="text-sm font-medium">Đến ngày</label>
              <Input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="mt-1"
              />
            </div>
            <div className="flex items-end">
              <Button onClick={fetchReports}>Áp dụng</Button>
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
          {/* Sales Report */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="flex items-center gap-2">
                    <BarChart3 className="h-5 w-5" />
                    Báo cáo doanh số
                  </CardTitle>
                  <CardDescription>Thống kê doanh thu và đơn hàng</CardDescription>
                </div>
                <Button variant="outline" onClick={handleExportSales}>
                  <Download className="mr-2 h-4 w-4" />
                  Xuất Excel
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {salesReport && (
                <div className="grid gap-4 md:grid-cols-3">
                  <div>
                    <p className="text-sm text-muted-foreground">Tổng doanh thu</p>
                    <p className="text-2xl font-bold text-primary">
                      {formatCurrency(salesReport.totalRevenue)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Tổng đơn hàng</p>
                    <p className="text-2xl font-bold">{salesReport.totalOrders}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Tăng trưởng</p>
                    <p className="text-2xl font-bold flex items-center gap-1">
                      <TrendingUp className="h-5 w-5" />
                      {salesReport.growthRate.toFixed(2)}%
                    </p>
                  </div>
                </div>
              )}
              {salesReport?.topProducts && salesReport.topProducts.length > 0 && (
                <div className="mt-6">
                  <h3 className="text-lg font-semibold mb-4">Sản phẩm bán chạy</h3>
                  <div className="space-y-2">
                    {salesReport.topProducts.map((product) => (
                      <div key={product.productId} className="flex items-center justify-between p-3 border rounded-lg">
                        <div>
                          <p className="font-medium">{product.productName}</p>
                          <p className="text-sm text-muted-foreground">
                            {product.unitsSold} đơn vị
                          </p>
                        </div>
                        <p className="font-bold text-primary">
                          {formatCurrency(product.revenue)}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Inventory Report */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="flex items-center gap-2">
                    <Package className="h-5 w-5" />
                    Báo cáo tồn kho
                  </CardTitle>
                  <CardDescription>Thống kê tồn kho và cảnh báo</CardDescription>
                </div>
                <Button variant="outline" onClick={handleExportInventory}>
                  <Download className="mr-2 h-4 w-4" />
                  Xuất Excel
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {inventoryReport && (
                <div className="grid gap-4 md:grid-cols-3">
                  <div>
                    <p className="text-sm text-muted-foreground">Tổng sản phẩm</p>
                    <p className="text-2xl font-bold">{inventoryReport.totalProducts}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Tồn kho thấp</p>
                    <p className="text-2xl font-bold text-yellow-600">
                      {inventoryReport.lowStockCount}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Hết hàng</p>
                    <p className="text-2xl font-bold text-red-600">
                      {inventoryReport.outOfStockCount}
                    </p>
                  </div>
                </div>
              )}
              {inventoryReport?.alerts && inventoryReport.alerts.length > 0 && (
                <div className="mt-6">
                  <h3 className="text-lg font-semibold mb-4">Cảnh báo</h3>
                  <div className="space-y-2">
                    {inventoryReport.alerts.map((alert, index) => (
                      <div key={index} className="p-3 border rounded-lg bg-yellow-50">
                        <p className="font-medium">{alert.productName}</p>
                        <p className="text-sm text-muted-foreground">{alert.message}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Revenue Report */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <DollarSign className="h-5 w-5" />
                <CardTitle>Báo cáo doanh thu</CardTitle>
              </div>
              <CardDescription>Thống kê doanh thu và thanh toán</CardDescription>
            </CardHeader>
            <CardContent>
              {revenueReport && (
                <div className="grid gap-4 md:grid-cols-3">
                  <div>
                    <p className="text-sm text-muted-foreground">Tổng doanh thu</p>
                    <p className="text-2xl font-bold text-primary">
                      {formatCurrency(revenueReport.totalRevenue)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Đã hoàn thành</p>
                    <p className="text-2xl font-bold text-green-600">
                      {formatCurrency(revenueReport.totalCompleted)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Đang chờ</p>
                    <p className="text-2xl font-bold text-yellow-600">
                      {formatCurrency(revenueReport.totalPending)}
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}


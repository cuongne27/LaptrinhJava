"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  Package,
  Users,
  ShoppingCart,
  FileText,
  CreditCard,
  Calendar,
  Truck,
  Building2,
  Tag,
  BarChart3,
  Settings,
  HelpCircle,
  LogOut,
  Zap,
  Warehouse,
  FileCheck,
  TrendingUp,
  ChevronLeft,
  ChevronRight,
  Menu,
  GitCompare,
} from "lucide-react";
import { useAuthStore } from "@/store/authStore";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";

const menuItems = [
  {
    title: "Tổng quan",
    icon: LayoutDashboard,
    href: "/dashboard",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Sản phẩm",
    icon: Package,
    href: "/products",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "So sánh sản phẩm",
    icon: GitCompare,
    href: "/product-comparison",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF", "CUSTOMER"],
  },
  {
    title: "Khách hàng",
    icon: Users,
    href: "/customers",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Đơn hàng",
    icon: ShoppingCart,
    href: "/orders",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Báo giá",
    icon: FileText,
    href: "/quotations",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Thanh toán",
    icon: CreditCard,
    href: "/payments",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Lịch hẹn",
    icon: Calendar,
    href: "/appointments",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF", "CUSTOMER"],
  },
  {
    title: "Kho hàng",
    icon: Warehouse,
    href: "/inventory",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Xe",
    icon: Truck,
    href: "/vehicles",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Đại lý",
    icon: Building2,
    href: "/dealers",
    roles: ["ADMIN", "BRAND_MANAGER"],
  },
  {
    title: "Thương hiệu",
    icon: Zap,
    href: "/brands",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Hợp đồng",
    icon: FileCheck,
    href: "/contracts",
    roles: ["ADMIN", "BRAND_MANAGER"],
  },
  {
    title: "Đặt hàng từ Hãng",
    icon: TrendingUp,
    href: "/sell-in-requests",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Khuyến mãi",
    icon: Tag,
    href: "/promotions",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF"],
  },
  {
    title: "Hỗ trợ",
    icon: HelpCircle,
    href: "/support-tickets",
    roles: ["ADMIN", "BRAND_MANAGER", "DEALER_STAFF", "CUSTOMER"],
  },
  {
    title: "Báo cáo",
    icon: BarChart3,
    href: "/reports",
    roles: ["ADMIN", "BRAND_MANAGER"],
  },
  {
    title: "Người dùng",
    icon: Users,
    href: "/users",
    roles: ["ADMIN"],
  },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuthStore();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    setMounted(true);
    // Load collapsed state from localStorage
    const savedCollapsed = localStorage.getItem("sidebarCollapsed");
    if (savedCollapsed === "true") {
      setCollapsed(true);
    }
  }, []);

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  const toggleCollapse = () => {
    setCollapsed(!collapsed);
    localStorage.setItem("sidebarCollapsed", (!collapsed).toString());
  };

  const filteredMenuItems = menuItems.filter((item) => {
    if (!user || !mounted) return false;
    // Handle both string array and object array for roles
    const userRoles = user.roles || [];
    const userRoleNames = userRoles.map((r: any) => {
      if (typeof r === "string") {
        // Remove "ROLE_" prefix if present (Spring Security format)
        return r.replace(/^ROLE_/, "").toUpperCase();
      }
      if (typeof r === "object" && r !== null) {
        const roleName = r.roleName || r.name || r.role || r;
        return typeof roleName === "string" ? roleName.replace(/^ROLE_/, "").toUpperCase() : String(roleName);
      }
      return String(r).replace(/^ROLE_/, "").toUpperCase();
    });
    
    // Check if user has any of the required roles
    const hasAccess = item.roles.some((requiredRole) => {
      const normalizedRequired = requiredRole.toUpperCase();
      return userRoleNames.some((userRole) => userRole === normalizedRequired);
    });
    
    return hasAccess;
  });

  // Group menu items by category
  const menuGroups = [
    {
      title: "Chính",
      items: filteredMenuItems.filter(
        (item) =>
          item.href === "/dashboard" ||
          item.href === "/products" ||
          item.href === "/product-comparison" ||
          item.href === "/customers" ||
          item.href === "/orders"
      ),
    },
    {
      title: "Bán hàng",
      items: filteredMenuItems.filter(
        (item) =>
          item.href === "/quotations" ||
          item.href === "/payments" ||
          item.href === "/appointments"
      ),
    },
    {
      title: "Quản lý",
      items: filteredMenuItems.filter(
        (item) =>
          item.href === "/inventory" ||
          item.href === "/vehicles" ||
          item.href === "/dealers" ||
          item.href === "/brands" ||
          item.href === "/contracts" ||
          item.href === "/sell-in-requests" ||
          item.href === "/promotions"
      ),
    },
    {
      title: "Hệ thống",
      items: filteredMenuItems.filter(
        (item) =>
          item.href === "/support-tickets" ||
          item.href === "/reports" ||
          item.href === "/users"
      ),
    },
  ].filter((group) => group.items.length > 0);

  if (!mounted) {
    return (
      <div className={cn("flex h-screen flex-col border-r bg-card transition-all", collapsed ? "w-16" : "w-64")}>
        <div className="flex h-16 items-center border-b px-6">
          <div className="flex items-center gap-2">
            <Zap className="h-6 w-6 text-primary" />
            {!collapsed && <span className="text-xl font-bold">EVM</span>}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={cn("flex h-screen flex-col border-r bg-card transition-all duration-300", collapsed ? "w-16" : "w-64")}>
      <div className="flex h-16 items-center justify-between border-b px-4">
        <div className="flex items-center gap-2">
          <Zap className="h-6 w-6 text-primary flex-shrink-0" />
          {!collapsed && <span className="text-xl font-bold">EVM</span>}
        </div>
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleCollapse}
          className="h-8 w-8"
        >
          {collapsed ? (
            <ChevronRight className="h-4 w-4" />
          ) : (
            <ChevronLeft className="h-4 w-4" />
          )}
        </Button>
      </div>

      <nav className="flex-1 space-y-2 p-4 overflow-y-auto">
        {menuGroups.map((group, groupIndex) => (
          <div key={groupIndex} className="space-y-1">
            {!collapsed && group.items.length > 0 && (
              <div className="px-3 py-2">
                <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                  {group.title}
                </p>
              </div>
            )}
            {group.items.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href || pathname.startsWith(item.href + "/");
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                    collapsed ? "justify-center" : "",
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                  )}
                  title={collapsed ? item.title : undefined}
                >
                  <Icon className="h-5 w-5 flex-shrink-0" />
                  {!collapsed && <span>{item.title}</span>}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>

      <div className="border-t p-4">
        {!collapsed && (
          <div className="mb-2 px-3 py-2 text-sm">
            <p className="font-medium truncate">{user?.fullName || user?.username}</p>
            <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
          </div>
        )}
        <Button
          variant="ghost"
          className={cn("w-full justify-start", collapsed ? "justify-center px-0" : "")}
          onClick={handleLogout}
          title={collapsed ? "Đăng xuất" : undefined}
        >
          <LogOut className={cn("h-4 w-4 flex-shrink-0", !collapsed && "mr-2")} />
          {!collapsed && <span>Đăng xuất</span>}
        </Button>
      </div>
    </div>
  );
}


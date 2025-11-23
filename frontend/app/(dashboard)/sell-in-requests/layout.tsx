import { DashboardLayout } from "@/components/layout/DashboardLayout";

export default function SellInRequestsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <DashboardLayout>{children}</DashboardLayout>;
}


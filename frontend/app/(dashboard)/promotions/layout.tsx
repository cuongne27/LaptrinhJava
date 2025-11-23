import { DashboardLayout } from "@/components/layout/DashboardLayout";

export default function PromotionsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <DashboardLayout>{children}</DashboardLayout>;
}


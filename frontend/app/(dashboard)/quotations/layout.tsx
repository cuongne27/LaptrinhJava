import { DashboardLayout } from "@/components/layout/DashboardLayout";

export default function QuotationsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <DashboardLayout>{children}</DashboardLayout>;
}


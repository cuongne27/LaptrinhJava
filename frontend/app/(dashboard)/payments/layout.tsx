import { DashboardLayout } from "@/components/layout/DashboardLayout";

export default function PaymentsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <DashboardLayout>{children}</DashboardLayout>;
}


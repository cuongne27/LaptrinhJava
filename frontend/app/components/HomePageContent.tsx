'use client';

import React from "react";
import Header from "./layout/Header";
import Container from "./layout/Container";
import OverviewSection from "./OverviewSection";
import DashboardPlaceholdersSection from "./DashboardPlaceholdersSection";
import QuickNavigationSection from "./QuickNavigationSection";

const QUICK_NAV_ITEMS = [
  { name: "Appointment", href: "/entities/appointment" },
  { name: "Inventory", href: "/entities/inventory" },
  { name: "ProductVariant", href: "/entities/product-variant" },
  { name: "SellInRequest", href: "/entities/sell-in-request" },
  { name: "VehicleHistory", href: "/entities/vehicle-history" },
  { name: "Brand", href: "/entities/brand" },
  { name: "OrderPromotions", href: "/entities/order-promotions" },
  { name: "Promotion", href: "/entities/promotion" },
  { name: "SellInRequestDetails", href: "/entities/sell-in-request-details" },
  { name: "VehicleServiceHistory", href: "/entities/vehicle-service-history" },
  { name: "Payment", href: "/entities/payment" },
  { name: "Role", href: "/entities/role" },
  { name: "SupportTicket", href: "/entities/support-ticket" },
  { name: "Dealer", href: "/entities/dealer" },
  { name: "PaymentContext", href: "/entities/payment-context" },
  { name: "RoleType", href: "/entities/role-type" },
  { name: "TechnicalSpecs", href: "/entities/technical-specs" },
  { name: "DealerContract", href: "/entities/dealer-contract" },
  { name: "Product", href: "/entities/product" },
  { name: "SalesOrder", href: "/entities/sales-order" },
  { name: "User", href: "/entities/user" },
  { name: "ProductFeature", href: "/entities/product-feature" },
  { name: "SalesOrderDetails", href: "/entities/sales-order-details"},
  { name: "Vehicle", href: "/entities/vehicles" },
  { name: "Customer", href: "/entities/customers" },
];

export default function HomePageContent() {
  return (
    <div className="flex min-h-screen w-full flex-col bg-slate-100">
      <Header />

      <main className="flex-1 py-12">
        <Container>
          <div className="flex flex-col gap-10">
            <OverviewSection />

            <DashboardPlaceholdersSection />

            <QuickNavigationSection items={QUICK_NAV_ITEMS} />
          </div>
        </Container>
      </main>

    </div>
  );
}

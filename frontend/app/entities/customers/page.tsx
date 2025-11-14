"use client";

import React from "react";
import CustomerListContent from "../../components/customer/CustomerListContent";
import ProtectedRoute from "../../components/auth/ProtectedRoute";
import type { FilterOption } from "../../components/filter/FilterDropdown";

const SORT_OPTIONS: FilterOption[] = [
  { value: "name_asc", label: "Tên A-Z" },
  { value: "name_desc", label: "Tên Z-A" },
  { value: "created_asc", label: "Mới nhất" },
  { value: "created_desc", label: "Cũ nhất" },
];

export default function CustomersPage() {
  return (
    <ProtectedRoute>
      <CustomerListContent sortOptions={SORT_OPTIONS} />
    </ProtectedRoute>
  );
}


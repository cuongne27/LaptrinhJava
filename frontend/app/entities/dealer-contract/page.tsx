"use client";
    
import React from "react";
import DealerContractListContent from "../../components/dealer-contract/DealerContractListContent";
import ProtectedRoute from "../../components/auth/ProtectedRoute";
import type { FilterOption } from "../../components/filter/FilterDropdown";

const SORT_OPTIONS: FilterOption[] = [
  { value: "start_date_asc", label: "Ngày bắt đầu cũ nhất" },
  { value: "start_date_desc", label: "Ngày bắt đầu mới nhất" },
  { value: "end_date_asc", label: "Ngày kết thúc sớm nhất" },
  { value: "end_date_desc", label: "Ngày kết thúc muộn nhất" },
];

export default function ContractsPage() {
  return (
    <ProtectedRoute>
      <DealerContractListContent sortOptions={SORT_OPTIONS} />
    </ProtectedRoute>
  );
}
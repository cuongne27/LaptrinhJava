import React from "react";
import VehicleListContent from "../../components/vehicle/VehicleListContent";
import type { FilterOption } from "../../components/filter/FilterDropdown";

const SORT_OPTIONS: FilterOption[] = [
  { value: "date_desc", label: "Mới nhất" },
  { value: "date_asc", label: "Cũ nhất" },
  { value: "name_asc", label: "Tên A-Z" },
  { value: "name_desc", label: "Tên Z-A" },
];

export default function InventoryPage() {
  return <VehicleListContent pageType="inventory" sortOptions={SORT_OPTIONS} />;
}


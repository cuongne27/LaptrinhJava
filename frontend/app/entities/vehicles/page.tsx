import React from "react";
import VehicleListContent from "../../components/vehicle/VehicleListContent";
import type { FilterOption } from "../../components/filter/FilterDropdown";

const SORT_OPTIONS: FilterOption[] = [
  { value: "date_desc", label: "Mới nhất" },
  { value: "date_asc", label: "Cũ nhất" },
  { value: "status_asc", label: "A - Z" },
  { value: "status_desc", label: "Z - A" },
];

export default function VehiclesPage() {
  return <VehicleListContent pageType="vehicles" sortOptions={SORT_OPTIONS} />;
}


import React from "react";
import InventoryFilterBar from "./InventoryFilterBar";

interface VehicleInventoryToolbarProps {
  onSearch: (value: string) => void;
  searchValue: string;
  onSortChange: (value: string) => void;
  selectedSort?: string;
  sortOptions: { value: string; label: string }[];
  onManageVehicles?: () => void;
}

export default function VehicleInventoryToolbar({
  onSearch,
  searchValue,
  onSortChange,
  selectedSort,
  sortOptions,
  onManageVehicles,
}: VehicleInventoryToolbarProps) {
  return (
    <div className="relative z-10 flex flex-col gap-3 rounded-3xl border-soft bg-surface p-4 shadow-sm backdrop-blur md:flex-row md:items-center md:justify-between">
      <InventoryFilterBar
        onSearch={onSearch}
        searchValue={searchValue}
        onSortChange={onSortChange}
        selectedSort={selectedSort}
        sortOptions={sortOptions}
        searchPlaceholder="Tên xe, VIN hoặc model"
        className="flex-1"
      />

      <button
        type="button"
        onClick={() => onManageVehicles?.()}
        className="flex h-11 items-center justify-center gap-2 rounded-2xl bg-primary px-6 py-2 text-sm font-semibold text-on-primary shadow-md transition hover:bg-primary-hover"
      >
        <span>Quản lý xe</span>
      </button>
    </div>
  );
}

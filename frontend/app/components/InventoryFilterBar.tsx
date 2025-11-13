import React from "react";
import VehicleSearchBar from "./VehicleSearchBar";
import VehicleFilterButton, { FilterOption } from "./VehicleFilterButton";

interface InventoryFilterBarProps {
  searchPlaceholder?: string;
  searchValue?: string;
  onSearch: (value: string) => void;
  sortOptions: FilterOption[];
  selectedSort?: string;
  onSortChange: (value: string) => void;
  className?: string;
}

export default function InventoryFilterBar({
  searchPlaceholder = "Tên SP",
  searchValue = "",
  onSearch,
  sortOptions,
  selectedSort,
  onSortChange,
  className = "",
}: InventoryFilterBarProps) {
  return (
    <div
      className={[
        "flex flex-col gap-3 text-zinc-700 md:flex-row md:items-center md:justify-between",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="flex flex-1 items-center gap-4">
        <VehicleSearchBar
          placeholder={searchPlaceholder}
          defaultValue={searchValue}
          onSearch={onSearch}
          className="max-w-lg"
        />

        <VehicleFilterButton
          options={sortOptions}
          selectedValue={selectedSort}
          onOptionSelect={onSortChange}
          label="Sắp xếp"
        />
      </div>
    </div>
  );
}


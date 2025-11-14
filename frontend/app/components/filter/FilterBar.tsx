"use client";

import React from "react";
import VehicleSearchBar from "../search/VehicleSearchBar";
import FilterDropdown, { FilterOption } from "./FilterDropdown";

interface FilterBarProps {
  // Search
  searchPlaceholder?: string;
  searchValue?: string;
  onSearch: (value: string) => void;
  
  // Sort
  sortOptions: FilterOption[];
  selectedSort?: string;
  onSortChange: (value: string) => void;
  
  // Additional filters for Inventory page
  showInventoryFilters?: boolean;
  modelOptions?: FilterOption[];
  selectedModel?: string;
  onModelChange?: (value: string) => void;
  vinOptions?: FilterOption[];
  selectedVin?: string;
  onVinChange?: (value: string) => void;
  inventoryStatusOptions?: FilterOption[];
  selectedInventoryStatus?: string;
  onInventoryStatusChange?: (value: string) => void;
  warehouseOptions?: FilterOption[];
  selectedWarehouse?: string;
  onWarehouseChange?: (value: string) => void;
  
  // Additional filters for Vehicles page
  showVehicleFilters?: boolean;
  productOptions?: FilterOption[];
  selectedProduct?: string;
  onProductChange?: (value: string) => void;
  colorOptions?: FilterOption[];
  selectedColor?: string;
  onColorChange?: (value: string) => void;
  vehicleStatusOptions?: FilterOption[];
  selectedVehicleStatus?: string;
  onVehicleStatusChange?: (value: string) => void;
  dealerOptions?: FilterOption[];
  selectedDealer?: string;
  onDealerChange?: (value: string) => void;
  
  className?: string;
}

export default function FilterBar({
  searchPlaceholder = "Tìm kiếm...",
  searchValue = "",
  onSearch,
  sortOptions,
  selectedSort,
  onSortChange,
  showInventoryFilters = false,
  modelOptions = [],
  selectedModel,
  onModelChange,
  vinOptions = [],
  selectedVin,
  onVinChange,
  inventoryStatusOptions = [],
  selectedInventoryStatus,
  onInventoryStatusChange,
  warehouseOptions = [],
  selectedWarehouse,
  onWarehouseChange,
  showVehicleFilters = false,
  productOptions = [],
  selectedProduct,
  onProductChange,
  colorOptions = [],
  selectedColor,
  onColorChange,
  vehicleStatusOptions = [],
  selectedVehicleStatus,
  onVehicleStatusChange,
  dealerOptions = [],
  selectedDealer,
  onDealerChange,
  className = "",
}: FilterBarProps) {
  return (
    <div
      className={[
        "flex flex-col gap-3 rounded-3xl border border-soft bg-surface p-4 shadow-sm backdrop-blur",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {/* Inventory filters row */}
      {showInventoryFilters && (
        <div className="flex flex-wrap items-center gap-3">
          <FilterDropdown
            options={modelOptions}
            selectedValue={selectedModel}
            onOptionSelect={(value) => onModelChange?.(value)}
            placeholder="Mẫu xe"
            className="min-w-[150px]"
          />
          <FilterDropdown
            options={vinOptions}
            selectedValue={selectedVin}
            onOptionSelect={(value) => onVinChange?.(value)}
            placeholder="VIN"
            className="min-w-[150px]"
          />
          <FilterDropdown
            options={inventoryStatusOptions}
            selectedValue={selectedInventoryStatus}
            onOptionSelect={(value) => onInventoryStatusChange?.(value)}
            placeholder="Trạng Thái"
            className="min-w-[150px]"
          />
          <FilterDropdown
            options={warehouseOptions}
            selectedValue={selectedWarehouse}
            onOptionSelect={(value) => onWarehouseChange?.(value)}
            placeholder="Kho"
            className="min-w-[150px]"
          />
        </div>
      )}

      {/* Vehicle filters row */}
      {showVehicleFilters && (
        <div className="flex flex-wrap items-center gap-3">
          {productOptions.length > 0 && (
            <FilterDropdown
              options={productOptions}
              selectedValue={selectedProduct}
              onOptionSelect={(value) => onProductChange?.(value)}
              placeholder="Mẫu xe"
              className="min-w-[150px]"
            />
          )}
          {colorOptions.length > 0 && (
            <FilterDropdown
              options={colorOptions}
              selectedValue={selectedColor}
              onOptionSelect={(value) => onColorChange?.(value)}
              placeholder="Màu sắc"
              className="min-w-[150px]"
            />
          )}
          {vehicleStatusOptions.length > 0 && (
            <FilterDropdown
              options={vehicleStatusOptions}
              selectedValue={selectedVehicleStatus}
              onOptionSelect={(value) => onVehicleStatusChange?.(value)}
              placeholder="Trạng thái"
              className="min-w-[150px]"
            />
          )}
          {dealerOptions.length > 0 && (
            <FilterDropdown
              options={dealerOptions}
              selectedValue={selectedDealer}
              onOptionSelect={(value) => onDealerChange?.(value)}
              placeholder="Đại lý"
              className="min-w-[150px]"
            />
          )}
        </div>
      )}

      {/* Search and Sort row */}
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="flex flex-1 items-center gap-4">
          <VehicleSearchBar
            placeholder={searchPlaceholder}
            defaultValue={searchValue}
            onSearch={onSearch}
            className="max-w-lg"
          />
          <FilterDropdown
            options={sortOptions}
            selectedValue={selectedSort}
            onOptionSelect={onSortChange}
            placeholder="Sắp xếp"
            showIcon={true}
          />
        </div>
      </div>
    </div>
  );
}


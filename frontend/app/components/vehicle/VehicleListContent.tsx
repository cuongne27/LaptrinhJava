"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import Header from "../layout/Header";
import Container from "../layout/Container";
import FilterBar from "../filter/FilterBar";
import InventoryTable from "../table/InventoryTable";
import Pagination from "../pagination/Pagination";
import {
  InventoryTableColumn,
  InventoryTableRowData,
} from "../table/InventoryTableRow";
import type { FilterOption } from "../filter/FilterDropdown";

const PAGE_SIZE = 5;
const DEFAULT_IMAGE_PLACEHOLDER = "https://via.placeholder.com/120x80?text=No+Image";
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

interface VehicleListResponse {
  id: string;
  vin?: string;
  color?: string;
  manufactureDate?: string;
  status?: string;
  productId?: number;
  productName?: string;
  productVersion?: string;
  dealerId?: number;
  dealerName?: string;
  hasSalesOrder?: boolean;
  totalSupportTickets?: number;
}

interface VehiclesPageResponse {
  content: VehicleListResponse[];
  totalPages: number;
  totalElements: number;
  number: number;
}

interface ProductResponse {
  id: number;
  productName: string;
  version?: string;
}

interface DealerResponse {
  id: number;
  dealerName: string;
}

type PageType = "inventory" | "vehicles";

interface VehicleListContentProps {
  pageType: PageType;
  sortOptions: FilterOption[];
}

export default function VehicleListContent({
  pageType,
  sortOptions,
}: VehicleListContentProps) {
  const [rows, setRows] = useState<InventoryTableRowData[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  
  // For inventory: store all vehicles for client-side filtering
  const [allInventoryVehicles, setAllInventoryVehicles] = useState<VehicleListResponse[]>([]);
  
  // Search and sort
  const [searchKeyword, setSearchKeyword] = useState("");
  const [sortBy, setSortBy] = useState<string | undefined>();
  
  // Filters for Vehicles page
  const [selectedProduct, setSelectedProduct] = useState<string>("");
  const [selectedColor, setSelectedColor] = useState<string>("");
  const [selectedStatus, setSelectedStatus] = useState<string>("");
  const [selectedDealer, setSelectedDealer] = useState<string>("");
  
  // Filters for Inventory page (from existing data)
  const [selectedModel, setSelectedModel] = useState<string>("");
  const [selectedVin, setSelectedVin] = useState<string>("");
  const [selectedInventoryStatus, setSelectedInventoryStatus] = useState<string>("");
  const [selectedWarehouse, setSelectedWarehouse] = useState<string>("");
  
  // Filter options
  const [productOptions, setProductOptions] = useState<FilterOption[]>([]);
  const [colorOptions, setColorOptions] = useState<FilterOption[]>([]);
  const [statusOptions, setStatusOptions] = useState<FilterOption[]>([]);
  const [dealerOptions, setDealerOptions] = useState<FilterOption[]>([]);
  
  // Inventory filter options (from existing data)
  const [modelOptions, setModelOptions] = useState<FilterOption[]>([]);
  const [vinOptions, setVinOptions] = useState<FilterOption[]>([]);
  const [inventoryStatusOptions, setInventoryStatusOptions] = useState<FilterOption[]>([]);
  const [warehouseOptions, setWarehouseOptions] = useState<FilterOption[]>([]);

  // Fetch all vehicles for inventory filter options
  const fetchAllInventoryVehicles = useCallback(async () => {
    if (pageType !== "inventory") return;

    try {
      const token =
        typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

      const url = new URL(`${API_BASE_URL}/api/vehicles`);
      url.searchParams.set("page", "0");
      url.searchParams.set("size", "1000"); // Fetch large amount to get all options

      const response = await fetch(url.toString(), {
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: "include",
      });

      if (response.ok) {
        const data: VehiclesPageResponse = await response.json();
        setAllInventoryVehicles(data.content || []);
      }
    } catch (error) {
      console.error("Failed to fetch all inventory vehicles", error);
    }
  }, [pageType]);

  // Fetch filter options for Vehicles page
  const fetchFilterOptions = useCallback(async () => {
    if (pageType !== "vehicles") return;

    try {
      const token =
        typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

      // Fetch products
      const productsResponse = await fetch(`${API_BASE_URL}/api/products?size=1000`, {
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: "include",
      });

      if (productsResponse.ok) {
        const productsData = await productsResponse.json();
        const products: ProductResponse[] = productsData.content || [];
        setProductOptions(
          products.map((p) => ({
            value: String(p.id),
            label: `${p.productName}${p.version ? ` ${p.version}` : ""}`,
          }))
        );
      }

      // Fetch dealers
      const dealersResponse = await fetch(`${API_BASE_URL}/api/dealers?size=1000`, {
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: "include",
      });

      if (dealersResponse.ok) {
        const dealersData = await dealersResponse.json();
        const dealers: DealerResponse[] = dealersData.content || [];
        setDealerOptions(
          dealers.map((d) => ({
            value: String(d.id),
            label: d.dealerName,
          }))
        );
      }

      // Status options
      setStatusOptions([
        { value: "AVAILABLE", label: "Có sẵn" },
        { value: "IN_TRANSIT", label: "Đang vận chuyển" },
        { value: "RESERVED", label: "Đã đặt" },
        { value: "SOLD", label: "Đã bán" },
      ]);
    } catch (error) {
      console.error("Failed to fetch filter options", error);
    }
  }, [pageType]);

  useEffect(() => {
    if (pageType === "inventory") {
      fetchAllInventoryVehicles();
    } else {
      fetchFilterOptions();
    }
  }, [pageType, fetchAllInventoryVehicles, fetchFilterOptions]);

  // Update inventory filter options when allInventoryVehicles changes
  useEffect(() => {
    if (pageType === "inventory" && allInventoryVehicles.length > 0) {
      const allUniqueModels = new Set<string>();
      const allUniqueVins = new Set<string>();
      const allUniqueStatuses = new Set<string>();
      const allUniqueWarehouses = new Set<string>();

      allInventoryVehicles.forEach((vehicle) => {
        const modelName = [vehicle.productName, vehicle.productVersion]
          .filter(Boolean)
          .join(" ") || vehicle.id;
        // Calculate warehouse based on vehicle id to ensure consistency
        const warehouseIndex = vehicle.id.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0) % 3;
        const warehouse = `Kho ${String.fromCharCode(65 + warehouseIndex)}`;
        
        allUniqueModels.add(modelName);
        if (vehicle.vin) allUniqueVins.add(vehicle.vin);
        if (vehicle.status) allUniqueStatuses.add(vehicle.status);
        allUniqueWarehouses.add(warehouse);
      });

      setModelOptions(
        Array.from(allUniqueModels).map((m) => ({ value: m, label: m }))
      );
      setVinOptions(
        Array.from(allUniqueVins).map((v) => ({ value: v, label: v }))
      );
      setInventoryStatusOptions(
        Array.from(allUniqueStatuses).map((s) => ({
          value: s,
          label:
            s === "AVAILABLE"
              ? "Đang tồn"
              : s === "SOLD"
              ? "Đã bán"
              : s,
        }))
      );
      setWarehouseOptions(
        Array.from(allUniqueWarehouses).map((w) => ({ value: w, label: w }))
      );
    }
  }, [pageType, allInventoryVehicles]);

  const mapVehiclesToRows = useCallback(
    (vehicles: VehicleListResponse[]) => {
      const mappedRows = vehicles.map<InventoryTableRowData>((vehicle) => {
        const modelName = [vehicle.productName, vehicle.productVersion]
          .filter(Boolean)
          .join(" ") || vehicle.id;
        
        // Calculate warehouse based on vehicle id to ensure consistency
        const warehouseIndex = vehicle.id.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0) % 3;
        const warehouse = `Kho ${String.fromCharCode(65 + warehouseIndex)}`; // Kho A, B, C

        return {
          id: vehicle.id,
          name: vehicle.productName ?? vehicle.id,
          modelName: pageType === "inventory" ? modelName : undefined,
          vin: vehicle.vin ?? "-",
          color: vehicle.color ?? "-",
          arrivalDate:
            pageType === "inventory" && vehicle.manufactureDate
              ? new Date(vehicle.manufactureDate).toLocaleDateString("vi-VN")
              : undefined,
          condition: pageType === "inventory" ? "Tốt" : undefined,
          warehouse: pageType === "inventory" ? warehouse : undefined,
          status: vehicle.status ?? "AVAILABLE",
          statusLabel:
            vehicle.status === "AVAILABLE"
              ? "Đang tồn"
              : vehicle.status === "SOLD"
              ? "Đã bán"
              : vehicle.status ?? "-",
          dealerName: vehicle.dealerName ?? "-",
          actionHref: `/entities/vehicles/${vehicle.id}`,
          imageUrl: DEFAULT_IMAGE_PLACEHOLDER,
        };
      });


      return mappedRows;
    },
    [pageType]
  );

  const fetchVehicles = useCallback(
    async (page: number) => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

        const url = new URL(`${API_BASE_URL}/api/vehicles`);
        
        // For inventory, don't send search/filter to backend (filter client-side)
        // For vehicles, send all filters to backend
        if (pageType === "inventory") {
          // Fetch all data for client-side filtering (already done in fetchAllInventoryVehicles)
          // Just use allInventoryVehicles, skip API call
          let filteredVehicles = allInventoryVehicles.length > 0 
            ? [...allInventoryVehicles] 
            : [];

          // Apply search keyword
          if (searchKeyword) {
            const keyword = searchKeyword.toLowerCase();
            filteredVehicles = filteredVehicles.filter((v) => {
              const model = [v.productName, v.productVersion]
                .filter(Boolean)
                .join(" ")
                .toLowerCase();
              const vin = (v.vin || "").toLowerCase();
              return model.includes(keyword) || vin.includes(keyword) || v.id.toLowerCase().includes(keyword);
            });
          }

          // Apply filters
          if (selectedModel) {
            filteredVehicles = filteredVehicles.filter((v) => {
              const model = [v.productName, v.productVersion]
                .filter(Boolean)
                .join(" ");
              return model === selectedModel;
            });
          }
          if (selectedVin) {
            filteredVehicles = filteredVehicles.filter((v) => v.vin === selectedVin);
          }
          if (selectedInventoryStatus) {
            filteredVehicles = filteredVehicles.filter((v) => v.status === selectedInventoryStatus);
          }
          if (selectedWarehouse) {
            filteredVehicles = filteredVehicles.filter((v) => {
              const warehouseIndex = v.id.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0) % 3;
              const warehouse = `Kho ${String.fromCharCode(65 + warehouseIndex)}`;
              return warehouse === selectedWarehouse;
            });
          }

          // Paginate client-side
          const startIndex = (page - 1) * PAGE_SIZE;
          const endIndex = startIndex + PAGE_SIZE;
          const paginatedVehicles = filteredVehicles.slice(startIndex, endIndex);
          const totalFilteredPages = Math.ceil(filteredVehicles.length / PAGE_SIZE);
          
          setRows(mapVehiclesToRows(paginatedVehicles));
          setTotalPages(Math.max(totalFilteredPages, 1));
          setIsLoading(false);
          return;
        }
        
        // For vehicles page: use backend filtering
        url.searchParams.set("page", String(page - 1));
        url.searchParams.set("size", String(PAGE_SIZE));
        
        if (searchKeyword) {
          url.searchParams.set("searchKeyword", searchKeyword);
        }
        if (sortBy) {
          url.searchParams.set("sortBy", sortBy);
        }
        
        // Vehicle filters
        if (selectedProduct) {
          url.searchParams.set("productId", selectedProduct);
        }
        if (selectedColor) {
          url.searchParams.set("color", selectedColor);
        }
        if (selectedStatus) {
          url.searchParams.set("status", selectedStatus);
        }
        if (selectedDealer) {
          url.searchParams.set("dealerId", selectedDealer);
        }

        const response = await fetch(url.toString(), {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          credentials: "include",
        });

        if (!response.ok) {
          if (response.status === 401) {
            throw new Error(
              "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            );
          }

          throw new Error("Không thể tải dữ liệu xe. Vui lòng thử lại sau.");
        }

        const data: VehiclesPageResponse = await response.json();
        const vehicles = data.content ?? [];

        setRows(mapVehiclesToRows(vehicles));
        setTotalPages(Math.max(data.totalPages ?? 1, 1));
      } catch (error) {
        console.error("Failed to fetch vehicles", error);
        setRows([]);
        setTotalPages(1);
        if (error instanceof TypeError) {
          setErrorMessage(
            "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối hoặc đảm bảo backend đang chạy."
          );
        } else {
          setErrorMessage(error instanceof Error ? error.message : "Đã xảy ra lỗi.");
        }
      } finally {
        setIsLoading(false);
      }
    },
    [
      pageType,
      searchKeyword,
      sortBy,
      selectedProduct,
      selectedColor,
      selectedStatus,
      selectedDealer,
      selectedModel,
      selectedVin,
      selectedInventoryStatus,
      selectedWarehouse,
      allInventoryVehicles,
      mapVehiclesToRows,
    ]
  );

  useEffect(() => {
    fetchVehicles(currentPage);
  }, [fetchVehicles, currentPage]);

  const handleSearch = useCallback((keyword: string) => {
    setSearchKeyword(keyword);
    setCurrentPage(1);
  }, []);

  const handleSortChange = useCallback((value: string) => {
    setSortBy(value || undefined);
    setCurrentPage(1);
  }, []);

  // Extract unique colors from all vehicles for color filter
  useEffect(() => {
    if (pageType === "vehicles" && rows.length > 0) {
      const uniqueColors = new Set<string>();
      rows.forEach((row) => {
        if (row.color && row.color !== "-") {
          uniqueColors.add(row.color);
        }
      });
      setColorOptions(
        Array.from(uniqueColors).map((c) => ({ value: c, label: c }))
      );
    }
  }, [rows, pageType]);

  const columns: InventoryTableColumn[] = useMemo(() => {
    if (pageType === "inventory") {
      return [
        {
          key: "imageUrl",
          header: "Ảnh",
          className: "w-32",
          align: "center",
          render: (row) => (
            <div className="flex items-center justify-center">
              {row.imageUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={row.imageUrl}
                  alt={row.modelName ?? row.name}
                  className="h-14 w-20 rounded-lg object-cover shadow-inner"
                  loading="lazy"
                />
              ) : (
                <span className="text-xs text-muted">Không có ảnh</span>
              )}
            </div>
          ),
        },
        {
          key: "modelName",
          header: "Mẫu xe",
          className: "min-w-[160px]",
        },
        {
          key: "vin",
          header: "VIN",
          className: "min-w-[140px]",
        },
        {
          key: "color",
          header: "Màu",
          className: "min-w-[100px]",
        },
        {
          key: "arrivalDate",
          header: "Ngày nhập",
          className: "min-w-[120px]",
        },
        {
          key: "condition",
          header: "Tình trạng",
          className: "min-w-[120px]",
        },
        {
          key: "warehouse",
          header: "Vị trí kho",
          className: "min-w-[120px]",
        },
        {
          key: "statusLabel",
          header: "Trạng thái",
          className: "min-w-[120px]",
          render: (row) => (
            <span className="text-secondary">{row.statusLabel ?? "Đang tồn"}</span>
          ),
        },
        {
          key: "actionHref",
          header: "Action",
          className: "w-24",
          align: "center",
          render: (row) => (
            <a
              href={row.actionHref ?? "#"}
              className="inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-xs font-semibold text-on-primary transition hover:bg-primary-hover"
            >
              Xem
            </a>
          ),
        },
      ];
    } else {
      return [
        {
          key: "imageUrl",
          header: "Ảnh",
          className: "w-32",
          align: "center",
          render: (row) => (
            <div className="flex items-center justify-center">
              {row.imageUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={row.imageUrl}
                  alt={row.name}
                  className="h-14 w-20 rounded-lg object-cover shadow-inner"
                  loading="lazy"
                />
              ) : (
                <span className="text-xs text-zinc-400">Không có ảnh</span>
              )}
            </div>
          ),
        },
        {
          key: "name",
          header: "Tên xe",
          className: "min-w-[160px]",
        },
        {
          key: "vin",
          header: "VIN",
          className: "min-w-[140px]",
        },
        {
          key: "status",
          header: "Trạng thái",
          className: "min-w-[120px]",
          render: (row) => (
            <span
              className={[
                "inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold",
                row.status === "AVAILABLE"
                  ? "status-available"
                  : row.status === "IN_TRANSIT"
                  ? "status-in-transit"
                  : row.status === "RESERVED"
                  ? "status-reserved"
                  : row.status === "SOLD"
                  ? "status-sold"
                  : "status-reserved",
              ]
                .filter(Boolean)
                .join(" ")}
            >
              {row.statusLabel ?? row.status ?? "Chưa rõ"}
            </span>
          ),
        },
        {
          key: "dealerName",
          header: "Đại lý",
          className: "min-w-[140px]",
        },
        {
          key: "actionHref",
          header: "Action",
          className: "w-24",
          align: "center",
          render: (row) => (
            <a
              href={row.actionHref ?? "#"}
              className="inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-xs font-semibold text-on-primary transition hover:bg-primary-hover"
            >
              Xem
            </a>
          ),
        },
      ];
    }
  }, [pageType]);

  return (
    <div className="flex min-h-screen w-full flex-col bg-page">
      <Header />
      <main className="flex-1 py-10">
        <Container className="flex flex-col gap-6">
          <div className="relative z-10">
            <FilterBar
              searchPlaceholder={
                pageType === "inventory"
                  ? "Tên SP"
                  : "Tên xe, VIN hoặc model"
              }
              searchValue={searchKeyword}
              onSearch={handleSearch}
              sortOptions={sortOptions}
              selectedSort={sortBy}
              onSortChange={handleSortChange}
              showInventoryFilters={pageType === "inventory"}
              modelOptions={modelOptions}
              selectedModel={selectedModel}
              onModelChange={(value) => {
                setSelectedModel(value);
                setCurrentPage(1);
              }}
              vinOptions={vinOptions}
              selectedVin={selectedVin}
              onVinChange={(value) => {
                setSelectedVin(value);
                setCurrentPage(1);
              }}
              inventoryStatusOptions={inventoryStatusOptions}
              selectedInventoryStatus={selectedInventoryStatus}
              onInventoryStatusChange={(value) => {
                setSelectedInventoryStatus(value);
                setCurrentPage(1);
              }}
              warehouseOptions={warehouseOptions}
              selectedWarehouse={selectedWarehouse}
              onWarehouseChange={(value) => {
                setSelectedWarehouse(value);
                setCurrentPage(1);
              }}
              showVehicleFilters={pageType === "vehicles"}
              productOptions={productOptions}
              selectedProduct={selectedProduct}
              onProductChange={(value) => {
                setSelectedProduct(value);
                setCurrentPage(1);
              }}
              colorOptions={colorOptions}
              selectedColor={selectedColor}
              onColorChange={(value) => {
                setSelectedColor(value);
                setCurrentPage(1);
              }}
              vehicleStatusOptions={statusOptions}
              selectedVehicleStatus={selectedStatus}
              onVehicleStatusChange={(value) => {
                setSelectedStatus(value);
                setCurrentPage(1);
              }}
              dealerOptions={dealerOptions}
              selectedDealer={selectedDealer}
              onDealerChange={(value) => {
                setSelectedDealer(value);
                setCurrentPage(1);
              }}
            />
          </div>

          {errorMessage ? (
            <div className="rounded-3xl border border-soft bg-error px-6 py-4 text-sm text-error">
              {errorMessage}
            </div>
          ) : null}

          <div className="relative z-0">
            <InventoryTable rows={rows} columns={columns} isLoading={isLoading} />
          </div>

          {totalPages > 1 ? (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={(page) => setCurrentPage(page)}
            />
          ) : null}
        </Container>
      </main>
    </div>
  );
}

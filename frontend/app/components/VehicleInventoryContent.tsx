"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import Header from "./Header";
import Container from "./Container";
import VehicleInventoryToolbar from "./VehicleInventoryToolbar";
import InventoryTable from "./InventoryTable";
import Pagination from "./Pagination";
import {
  InventoryTableColumn,
  InventoryTableRowData,
} from "./InventoryTableRow";

import type { FilterOption } from "./VehicleFilterButton";

const PAGE_SIZE = 5;
const DEFAULT_IMAGE_PLACEHOLDER = "https://via.placeholder.com/120x80?text=No+Image";

const SORT_OPTIONS: FilterOption[] = [
  { value: "date_desc", label: "Mới nhất" },
  { value: "date_asc", label: "Cũ nhất" },
  { value: "status_asc", label: "A - Z" },
  { value: "status_desc", label: "Z - A" },
];

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
  number: number; // current page (0-indexed)
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function VehicleInventoryContent() {
  const [rows, setRows] = useState<InventoryTableRowData[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [sortBy, setSortBy] = useState<string | undefined>();

  const mapVehiclesToRows = useCallback((vehicles: VehicleListResponse[]) => {
    return vehicles.map<InventoryTableRowData>((vehicle) => ({
      id: vehicle.id,
      name:
        [vehicle.productName, vehicle.productVersion]
          .filter(Boolean)
          .join(" ") || vehicle.id,
      vin: vehicle.vin ?? "-",
      color: vehicle.color ?? "-",
      status: vehicle.status ?? (vehicle.hasSalesOrder ? "Đã bán" : "-"),
      dealerName: vehicle.dealerName ?? "-",
      priceLabel: vehicle.productVersion ?? "-",
      stockLabel: vehicle.status ?? "-",
      imageUrl: DEFAULT_IMAGE_PLACEHOLDER,
    }));
  }, []);

  const fetchVehicles = useCallback(
    async (page: number) => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

        const url = new URL(`${API_BASE_URL}/api/vehicles`);
        url.searchParams.set("page", String(page - 1));
        url.searchParams.set("size", String(PAGE_SIZE));
        if (searchKeyword) {
          url.searchParams.set("searchKeyword", searchKeyword);
        }
        if (sortBy) {
          url.searchParams.set("sortBy", sortBy);
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
    [mapVehiclesToRows, searchKeyword, sortBy]
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

  const columns: InventoryTableColumn[] = useMemo(
    () => [
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
            {row.status ?? "Chưa rõ"}
          </span>
        ),
      },
      {
        key: "dealerName",
        header: "Đại lý",
        className: "min-w-[140px]",
      },
    ],
    []
  );

  return (
    <div className="flex min-h-screen w-full flex-col bg-page">
      <Header />
      <main className="flex-1 py-10">
        <Container className="flex flex-col gap-6">
          <VehicleInventoryToolbar
            onSearch={handleSearch}
            searchValue={searchKeyword}
            onSortChange={handleSortChange}
            selectedSort={sortBy}
            sortOptions={SORT_OPTIONS}
          />

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


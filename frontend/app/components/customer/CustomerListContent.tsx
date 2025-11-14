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

const PAGE_SIZE = 10;
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

interface CustomerListResponse {
  id: number;
  fullName?: string;
  phoneNumber?: string;
  email?: string;
  address?: string;
  customerType?: string;
  createdAt?: string;
  totalOrders?: number;
  totalSupportTickets?: number;
}

interface CustomersPageResponse {
  content: CustomerListResponse[];
  totalPages: number;
  totalElements: number;
  number: number;
}

interface CustomerListContentProps {
  sortOptions: FilterOption[];
}

export default function CustomerListContent({
  sortOptions,
}: CustomerListContentProps) {
  const [rows, setRows] = useState<InventoryTableRowData[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Search and sort
  const [searchKeyword, setSearchKeyword] = useState("");
  const [sortBy, setSortBy] = useState<string | undefined>();

  // Filters
  const [selectedCustomerType, setSelectedCustomerType] = useState<string>("");

  // Filter options
  const [customerTypeOptions, setCustomerTypeOptions] = useState<FilterOption[]>([]);

  const mapCustomersToRows = useCallback((customers: CustomerListResponse[]) => {
    return customers.map<InventoryTableRowData>((customer) => ({
      id: String(customer.id),
      name: customer.fullName ?? "-",
      vin: customer.phoneNumber ?? "-",
      color: customer.email ?? "-",
      arrivalDate: customer.createdAt
        ? new Date(customer.createdAt).toLocaleDateString("vi-VN")
        : "-",
      condition: customer.customerType ?? "-",
      warehouse: customer.address ?? "-",
      status: customer.customerType ?? "-",
      statusLabel: customer.customerType ?? "-",
      dealerName: customer.totalOrders ? `${customer.totalOrders} đơn hàng` : "0 đơn hàng",
      priceLabel: customer.totalSupportTickets
        ? `${customer.totalSupportTickets} tickets`
        : "0 tickets",
      actionHref: `/entities/customers/${customer.id}`,
    }));
  }, []);

  // Fetch customer types from first page to populate filter options
  useEffect(() => {
    const fetchCustomerTypes = async () => {
      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

        const url = new URL(`${API_BASE_URL}/api/customers`);
        url.searchParams.set("page", "0");
        url.searchParams.set("size", "100"); // Get more to extract types

        const response = await fetch(url.toString(), {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          credentials: "include",
        });

        if (response.ok) {
          const data: CustomersPageResponse = await response.json();
          const customers = data.content ?? [];
          const uniqueTypes = new Set<string>();
          customers.forEach((customer) => {
            if (customer.customerType) {
              uniqueTypes.add(customer.customerType);
            }
          });
          setCustomerTypeOptions(
            Array.from(uniqueTypes).map((t) => ({ value: t, label: t }))
          );
        }
      } catch (error) {
        console.error("Failed to fetch customer types", error);
      }
    };

    fetchCustomerTypes();
  }, []);

  const fetchCustomers = useCallback(
    async (page: number) => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

        const url = new URL(`${API_BASE_URL}/api/customers`);
        url.searchParams.set("page", String(page - 1));
        url.searchParams.set("size", String(PAGE_SIZE));

        if (searchKeyword) {
          url.searchParams.set("searchKeyword", searchKeyword);
        }
        if (sortBy) {
          url.searchParams.set("sortBy", sortBy);
        }
        if (selectedCustomerType) {
          url.searchParams.set("customerType", selectedCustomerType);
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

          throw new Error("Không thể tải dữ liệu khách hàng. Vui lòng thử lại sau.");
        }

        const data: CustomersPageResponse = await response.json();
        const customers = data.content ?? [];

        setRows(mapCustomersToRows(customers));
        setTotalPages(Math.max(data.totalPages ?? 1, 1));
      } catch (error) {
        console.error("Failed to fetch customers", error);
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
    [searchKeyword, sortBy, selectedCustomerType, mapCustomersToRows]
  );

  useEffect(() => {
    fetchCustomers(currentPage);
  }, [fetchCustomers, currentPage]);

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
        key: "name",
        header: "Họ và tên",
        className: "min-w-[180px]",
      },
      {
        key: "vin",
        header: "Số điện thoại",
        className: "min-w-[140px]",
      },
      {
        key: "color",
        header: "Email",
        className: "min-w-[200px]",
      },
      {
        key: "warehouse",
        header: "Địa chỉ",
        className: "min-w-[200px]",
      },
      // {
      //   key: "condition",
      //   header: "Loại khách hàng",
      //   className: "min-w-[150px]",
      //   render: (row) => (
      //     <span className="text-secondary">{row.condition ?? "-"}</span>
      //   ),
      // },
      {
        key: "arrivalDate",
        header: "Ngày tạo",
        className: "min-w-[120px]",
      },
      {
        key: "dealerName",
        header: "Tổng đơn hàng",
        className: "min-w-[140px]",
        align: "center",
        render: (row) => (
          <span className="text-secondary">{row.dealerName ?? "0 đơn hàng"}</span>
        ),
      },
      {
        key: "priceLabel",
        header: "Tổng tickets",
        className: "min-w-[120px]",
        align: "center",
        render: (row) => (
          <span className="text-secondary">{row.priceLabel ?? "0 tickets"}</span>
        ),
      },
      // {
      //   key: "actionHref",
      //   header: "Action",
      //   className: "w-24",
      //   align: "center",
      //   render: (row) => (
      //     <a
      //       href={row.actionHref ?? "#"}
      //       className="inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-xs font-semibold text-on-primary transition hover:bg-primary-hover"
      //     >
      //       Xem
      //     </a>
      //   ),
      // },
    ],
    []
  );

  return (
    <div className="flex min-h-screen w-full flex-col bg-page">
      <Header />
      <main className="flex-1 py-10">
        <Container className="flex flex-col gap-6">
          <div className="relative z-10">
            <FilterBar
              searchPlaceholder="Tìm kiếm theo tên, SĐT, email, địa chỉ..."
              searchValue={searchKeyword}
              onSearch={handleSearch}
              sortOptions={sortOptions}
              selectedSort={sortBy}
              onSortChange={handleSortChange}
              showCustomerFilters={true}
              customerTypeOptions={customerTypeOptions}
              selectedCustomerType={selectedCustomerType}
              onCustomerTypeChange={(value) => {
                setSelectedCustomerType(value);
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


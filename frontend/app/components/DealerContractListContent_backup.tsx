"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import Header from "../layout/Header";
import Container from "../layout/Container";
import FilterBar from "../filter/FilterBar";
import InventoryTable from "../table/InventoryTable";
import Pagination from "../pagination/Pagination";
import type { FilterOption } from "../filter/FilterDropdown";
import {
  InventoryTableColumn,
  InventoryTableRow,
} from "../table/InventoryTable";

interface DealerContract {
  id: string;
  contractNumber: string;
  dealerName: string;
  startDate: string;
  endDate: string;
  status: string;
  value?: number;
}

interface DealerContractsPageResponse {
  content: DealerContract[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
const PAGE_SIZE = 10;

interface DealerContractListContentProps {
  sortOptions: FilterOption[];
}

export default function DealerContractListContent({
  sortOptions,
}: DealerContractListContentProps) {
  const [rows, setRows] = useState<InventoryTableRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState<string>("");
  const [sortBy, setSortBy] = useState<string | undefined>();
  const [selectedStatus, setSelectedStatus] = useState<string | undefined>();
  const [statusOptions, setStatusOptions] = useState<FilterOption[]>([]);

  const mapDealerContractsToRows = useCallback(
    (contracts: DealerContract[]): InventoryTableRow[] => {
      return contracts.map((contract) => ({
        id: contract.id,
        name: contract.contractNumber,
        vin: contract.dealerName,
        color: contract.status,
        warehouse: `${contract.startDate} - ${contract.endDate}`,
        condition: contract.value ? `${contract.value.toLocaleString()} VNÄ` : "-",
        arrivalDate: new Date(contract.startDate).toLocaleDateString("vi-VN"),
        dealerName: contract.status,
        priceLabel: contract.value ? `${contract.value.toLocaleString()} VNÄ` : "-",
        actionHref: `/entities/dealer-contract/${contract.id}`,
      }));
    },
    []
  );

  // Fetch status options
  useEffect(() => {
    const fetchStatusOptions = async () => {
      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
        const url = new URL(`${API_BASE_URL}/api/dealer-contracts`);
        url.searchParams.set("page", "0");
        url.searchParams.set("size", "100"); // Get more to extract types

        const response = await fetch(url.toString(), {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` : {}),
          },
          credentials: "include",
        });

        if (response.ok) {
          const data: DealerContractsPageResponse = await response.json();
          const contracts = data.content ?? [];
          const uniqueStatuses = new Set<string>();
          contracts.forEach((contract) => {
            if (contract.status) {
              uniqueStatuses.add(contract.status);
            }
          });
          setStatusOptions(
            Array.from(uniqueStatuses).map((status) => ({ value: status, label: status }))
          );
        }
      } catch (error) {
        console.error("Failed to fetch contract statuses", error);
      }
    };

    fetchStatusOptions();
  }, []);

  const fetchDealerContracts = useCallback(
    async (page: number) => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
        const url = new URL(`${API_BASE_URL}/api/dealer-contracts`);
        url.searchParams.set("page", String(page - 1));
        url.searchParams.set("size", String(PAGE_SIZE));

        if (searchKeyword) {
          url.searchParams.set("searchKeyword", searchKeyword);
        }
        if (sortBy) {
          url.searchParams.set("sortBy", sortBy);
        }
        if (selectedStatus) {
          url.searchParams.set("status", selectedStatus);
        }

        const response = await fetch(url.toString(), {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` : {}),
          },
          credentials: "include",
        });

        if (!response.ok) {
          if (response.status === 401) {
            throw new Error(
              "Báº¡n chÆ°a Ä‘Äƒng nháº­p hoáº·c phiÃªn Ä‘Äƒng nháº­p Ä‘Ã£ háº¿t háº¡n. Vui lÃ²ng Ä‘Äƒng nháº­p láº¡i."
            );
          }

          throw new Error("KhÃ´ng thá»ƒ táº£i dá»¯ liá»‡u há»£p Ä‘á»“ng. Vui lÃ²ng thá»­ láº¡i sau.");
        }

        const data: DealerContractsPageResponse = await response.json();
        const contracts = data.content ?? [];

        setRows(mapDealerContractsToRows(contracts));
        setTotalPages(Math.max(data.totalPages ?? 1, 1));
      } catch (error) {
        console.error("Failed to fetch dealer contracts", error);
        setRows([]);
        setTotalPages(1);
        if (error instanceof TypeError) {
          setErrorMessage(
            "KhÃ´ng thá»ƒ káº¿t ná»‘i tá»›i mÃ¡y chá»§. Vui lÃ²ng kiá»ƒm tra káº¿t ná»‘i hoáº·c Ä‘áº£m báº£o backend Ä‘ang cháº¡y."
          );
        } else {
          setErrorMessage(error instanceof Error ? error.message : "ÄÃ£ xáº£y ra lá»—i.");
        }
      } finally {
        setIsLoading(false);
      }
    },
    [searchKeyword, sortBy, selectedStatus, mapDealerContractsToRows]
  );

  useEffect(() => {
    fetchDealerContracts(currentPage);
  }, [fetchDealerContracts, currentPage]);

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
        header: "Sá»‘ há»£p Ä‘á»“ng",
        className: "min-w-[180px]",
      },
      {
        key: "vin",
        header: "Äáº¡i lÃ½",
        className: "min-w-[140px]",
      },
      {
        key: "color",
        header: "Tráº¡ng thÃ¡i",
        className: "min-w[120px]",
      },
      {
        key: "warehouse",
        header: "Thá»i háº¡n",
        className: "min-w-[200px]",
      },
      {
        key: "condition",
        header: "GiÃ¡ trá»‹",
        className: "min-w-[150px]",
        render: (row) => (
          <span className="text-secondary">{row.condition ?? "-"}</span>
        ),
      },
      {
        key: "arrivalDate",
        header: "NgÃ y báº¯t Ä‘áº§u",
        className: "min-w-[120px]",
      },
      {
        key: "dealerName",
        header: "Thao tÃ¡c",
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
              searchPlaceholder="TÃ¬m kiáº¿m theo sá»‘ há»£p Ä‘á»“ng, tÃªn Ä‘áº¡i lÃ½..."
              searchValue={searchKeyword}
              onSearch={handleSearch}
              sortOptions={sortOptions}
              selectedSort={sortBy}
              onSortChange={handleSortChange}
              showCustomerFilters={true}
              customerTypeOptions={statusOptions}
              selectedCustomerType={selectedStatus}
              onCustomerTypeChange={(value) => {
                setSelectedStatus(value);
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


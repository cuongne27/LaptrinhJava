"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Header from "../layout/Header";
import Container from "../layout/Container";
import FilterBar from "../filter/FilterBar";
import InventoryTable from "../table/InventoryTable";
import Pagination from "../pagination/Pagination";
import type { FilterOption } from "../filter/FilterDropdown";
import CrudActionButton from "../crud/CrudActionButton";
import CrudFormModal from "../crud/CrudFormModal";
import type {
  InventoryTableColumn,
  InventoryTableRowData,
} from "../table/InventoryTableRow";

const PAGE_SIZE = 10;
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

interface DealerContractListResponse {
  id: number;
  startDate: string;
  endDate: string;
  commissionRate: number;
  salesTarget: number;
  status: string;
  brandId: number;
  brandName: string;
  dealerId: number;
  dealerName: string;
  daysRemaining?: number;
  totalDays?: number;
  progressPercentage?: number;
}

interface DealerContractsPageResponse {
  content: DealerContractListResponse[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

interface DealerContractRequest {
  brandId: number;
  dealerId: number;
  startDate: string;
  endDate: string;
  contractTerms?: string;
  commissionRate: number;
  salesTarget: number;
}

interface DealerContractListContentProps {
  sortOptions: FilterOption[];
}

export default function DealerContractListContent({
  sortOptions,
}: DealerContractListContentProps) {
  const [rows, setRows] = useState<InventoryTableRowData[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Search and sort
  const [searchKeyword, setSearchKeyword] = useState("");
  const [sortBy, setSortBy] = useState<string | undefined>();

  // Filters
  const [selectedBrand, setSelectedBrand] = useState<string>("");
  const [selectedDealer, setSelectedDealer] = useState<string>("");
  const [selectedStatus, setSelectedStatus] = useState<string>("");

  // Filter options
  const [brandOptions, setBrandOptions] = useState<FilterOption[]>([]);
  const [dealerOptions, setDealerOptions] = useState<FilterOption[]>([]);
  const [statusOptions] = useState<FilterOption[]>([
    { value: "ACTIVE", label: "Đang hiệu lực" },
    { value: "EXPIRED", label: "Hết hạn" },
    { value: "UPCOMING", label: "Sắp bắt đầu" },
  ]);

  // CRUD Modal state
  const [isCrudModalOpen, setIsCrudModalOpen] = useState(false);
  const [crudMode, setCrudMode] = useState<"create" | "edit" | "delete">("create");
  const [selectedContract, setSelectedContract] = useState<DealerContractListResponse | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // API endpoints configuration
  const apiConfig = {
    baseUrl: `${API_BASE_URL}/api/dealer-contracts`,
    fields: [
      { name: "brandId", label: "Thương hiệu", type: "select", required: true },
      { name: "dealerId", label: "Đại lý", type: "select", required: true },
      { name: "startDate", label: "Ngày bắt đầu", type: "date", required: true },
      { name: "endDate", label: "Ngày kết thúc", type: "date", required: true },
      { name: "contractTerms", label: "Điều khoản hợp đồng", type: "textarea", required: false },
      { name: "commissionRate", label: "Tỷ lệ hoa hồng (%)", type: "number", required: true },
      { name: "salesTarget", label: "Mục tiêu doanh số", type: "number", required: true },
    ]
  };

  const mapContractsToRows = useCallback((contracts: DealerContractListResponse[]) => {
    return contracts.map<InventoryTableRowData>((contract) => ({
      id: String(contract.id),
      name: contract.brandName ?? "-",
      modelName: contract.dealerName ?? "-",
      vin: contract.commissionRate ? `${contract.commissionRate}%` : "-",
      color: contract.salesTarget ? `${contract.salesTarget.toLocaleString("vi-VN")} VNĐ` : "-",
      arrivalDate: contract.startDate
        ? new Date(contract.startDate).toLocaleDateString("vi-VN")
        : "-",
      condition: contract.endDate
        ? new Date(contract.endDate).toLocaleDateString("vi-VN")
        : "-",
      warehouse: contract.status ?? "-",
      status: contract.status,
      statusLabel: getStatusLabel(contract.status),
      dealerName:
        contract.daysRemaining !== undefined
          ? `${contract.daysRemaining} ngày còn lại`
          : "-",
      priceLabel:
        contract.progressPercentage !== undefined
          ? `${contract.progressPercentage}% tiến độ`
          : "-",
      actionHref: `/entities/dealer-contract/${contract.id}`,
    }));
  }, []);

  const getStatusLabel = (status: string): string => {
    switch (status) {
      case "ACTIVE": return "Đang hiệu lực";
      case "EXPIRED": return "Hết hạn";
      case "UPCOMING": return "Sắp bắt đầu";
      default: return status;
    }
  };

  const fetchContracts = useCallback(
    async (page: number) => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const url = new URL(`${API_BASE_URL}/api/dealer-contracts`);
        url.searchParams.set("page", String(page - 1));
        url.searchParams.set("size", String(PAGE_SIZE));

        if (searchKeyword) {
          url.searchParams.set("search", searchKeyword);
        }
        if (sortBy) {
          url.searchParams.set("sortBy", sortBy);
        }
        if (selectedBrand) {
          url.searchParams.set("brandId", selectedBrand);
        }
        if (selectedDealer) {
          url.searchParams.set("dealerId", selectedDealer);
        }
        if (selectedStatus) {
          url.searchParams.set("status", selectedStatus);
        }

        const response = await fetch(url.toString());

        if (!response.ok) {
          throw new Error("Không thể tải dữ liệu hợp đồng. Vui lòng thử lại.");
        }

        const data: DealerContractsPageResponse = await response.json();
        const contracts = data.content ?? [];

        setRows(mapContractsToRows(contracts));
        setTotalPages(Math.max(data.totalPages ?? 1, 1));
      } catch (error) {
        console.error("Error fetching contracts:", error);
        setRows([]);
        setTotalPages(1);
        setErrorMessage(
          error instanceof Error
            ? error.message
            : "Không thể tải dữ liệu hợp đồng. Vui lòng thử lại.",
        );
      } finally {
        setIsLoading(false);
      }
    },
    [searchKeyword, sortBy, selectedBrand, selectedDealer, selectedStatus, mapContractsToRows],
  );

  const fetchFilterOptions = useCallback(async () => {
    try {
      // Fetch brands
      const brandsResponse = await fetch(`${API_BASE_URL}/api/brands`);
      if (brandsResponse.ok) {
        const brands = await brandsResponse.json();
        setBrandOptions(brands.map((brand: any) => ({
          value: String(brand.id),
          label: brand.brandName,
        })));
      }

      // Fetch dealers
      const dealersResponse = await fetch(`${API_BASE_URL}/api/dealers`);
      if (dealersResponse.ok) {
        const dealers = await dealersResponse.json();
        setDealerOptions(dealers.map((dealer: any) => ({
          value: String(dealer.id),
          label: dealer.dealerName,
        })));
      }
    } catch (error) {
      console.error("Error fetching filter options:", error);
    }
  }, []);

  const handleCrudAction = (mode: "create" | "edit" | "delete", contract?: DealerContractListResponse) => {
    setCrudMode(mode);
    setSelectedContract(contract || null);
    setIsCrudModalOpen(true);
  };

  const handleCrudSubmit = async (formData: any) => {
    try {
      setIsSubmitting(true);
      
      let url = apiConfig.baseUrl;
      let method = "POST";
      
      if (crudMode === "edit" && selectedContract) {
        url += `/${selectedContract.id}`;
        method = "PUT";
      } else if (crudMode === "delete" && selectedContract) {
        url += `/${selectedContract.id}`;
        method = "DELETE";
      }

      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
        },
        body: crudMode !== "delete" ? JSON.stringify(formData) : undefined,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      setIsCrudModalOpen(false);
      fetchContracts(); // Refresh data
    } catch (error) {
      console.error("Error performing CRUD operation:", error);
      setErrorMessage("Không thể thực hiện thao tác. Vui lòng thử lại.");
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    fetchContracts(currentPage);
  }, [fetchContracts, currentPage]);

  useEffect(() => {
    fetchFilterOptions();
  }, [fetchFilterOptions]);

  const columns: InventoryTableColumn[] = useMemo(
    () => [
      { key: "name", header: "Thương hiệu", className: "min-w-[160px]" },
      { key: "modelName", header: "Đại lý", className: "min-w-[160px]" },
      { key: "vin", header: "Hoa hồng", align: "center", className: "min-w-[120px]" },
      {
        key: "color",
        header: "Mục tiêu doanh số",
        align: "right",
        className: "min-w-[160px]",
      },
      { key: "arrivalDate", header: "Ngày bắt đầu", align: "center", className: "min-w-[130px]" },
      { key: "condition", header: "Ngày kết thúc", align: "center", className: "min-w-[130px]" },
      { key: "warehouse", header: "Trạng thái", align: "center", className: "min-w-[120px]" },
      {
        key: "dealerName",
        header: "Thời gian còn lại",
        align: "center",
        className: "min-w-[150px]",
      },
      {
        key: "priceLabel",
        header: "Tiến độ",
        align: "center",
        className: "min-w-[130px]",
      },
    ],
    [],
  );

  const handleSearch = useCallback((keyword: string) => {
    setSearchKeyword(keyword);
    setCurrentPage(1);
  }, []);

  const handleSortChange = useCallback((value: string) => {
    setSortBy(value || undefined);
    setCurrentPage(1);
  }, []);

  return (
    <div className="flex min-h-screen w-full flex-col bg-page">
      <Header />
      <main className="flex-1 py-10">
        <Container className="flex flex-col gap-6">
          <div className="relative z-10">
            <FilterBar
              searchPlaceholder="Tìm kiếm hợp đồng..."
              searchValue={searchKeyword}
              onSearch={handleSearch}
              sortOptions={sortOptions}
              selectedSort={sortBy}
              onSortChange={handleSortChange}
              showVehicleFilters={true}
              productOptions={brandOptions}
              selectedProduct={selectedBrand}
              onProductChange={(value) => {
                setSelectedBrand(value);
                setCurrentPage(1);
              }}
              dealerOptions={dealerOptions}
              selectedDealer={selectedDealer}
              onDealerChange={(value) => {
                setSelectedDealer(value);
                setCurrentPage(1);
              }}
              vehicleStatusOptions={statusOptions}
              selectedVehicleStatus={selectedStatus}
              onVehicleStatusChange={(value) => {
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

            <CrudActionButton
              onClick={() => handleCrudAction("create")}
              className="absolute bottom-4 right-4 z-20"
            />
          </div>

          {totalPages > 1 ? (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={(page) => setCurrentPage(page)}
            />
          ) : null}

          <CrudFormModal
            isOpen={isCrudModalOpen}
            onClose={() => setIsCrudModalOpen(false)}
            mode={crudMode}
            title={`${crudMode === "create" ? "Thêm" : crudMode === "edit" ? "Sửa" : "Xóa"} hợp đồng đại lý`}
            fields={apiConfig.fields.map((field) => ({
              ...field,
              type: field.type as "number" | "date" | "text" | "select" | "textarea",
            }))}
            initialData={selectedContract}
            onSubmit={handleCrudSubmit}
            isSubmitting={isSubmitting}
          />
        </Container>
      </main>
    </div>
  );
}

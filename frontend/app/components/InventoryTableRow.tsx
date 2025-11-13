import React from "react";

export interface InventoryTableRowData {
  id: string;
  name: string;
  modelName?: string;
  vin?: string;
  color?: string;
  arrivalDate?: string;
  condition?: string;
  warehouse?: string;
  status?: string;
  statusLabel?: string;
  actionHref?: string;
  dealerName?: string;
  priceLabel?: string;
  stockLabel?: string | number;
  imageUrl?: string;
  imageAlt?: string;
}

export interface InventoryTableColumn {
  key: keyof InventoryTableRowData;
  header: string;
  className?: string;
  align?: "left" | "center" | "right";
  render?: (row: InventoryTableRowData) => React.ReactNode;
}

interface InventoryTableRowProps {
  row: InventoryTableRowData;
  columns: InventoryTableColumn[];
  isEven?: boolean;
}

export default function InventoryTableRow({
  row,
  columns,
  isEven,
}: InventoryTableRowProps) {
  return (
    <tr
      className={[
        "transition-colors hover:bg-surface",
        isEven ? "table-row-even" : "table-row-odd",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {columns.map((column) => {
        const value = row[column.key as keyof InventoryTableRowData];

        return (
          <td
            key={`${row.id}-${String(column.key)}`}
            className={[
              "px-4 py-4 text-sm font-semibold text-secondary",
              column.align === "center" ? "text-center" : "",
              column.align === "right" ? "text-right" : "",
              column.className ?? "",
            ]
              .filter(Boolean)
              .join(" ")}
          >
            {column.render ? column.render(row) : value ?? "-"}
          </td>
        );
      })}
    </tr>
  );
}


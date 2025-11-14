import React from "react";
import InventoryTableRow, {
  InventoryTableColumn,
  InventoryTableRowData,
} from "./InventoryTableRow";

interface InventoryTableProps {
  rows: InventoryTableRowData[];
  columns: InventoryTableColumn[];
  isLoading?: boolean;
}

export default function InventoryTable({
  rows,
  columns,
  isLoading = false,
}: InventoryTableProps) {
  const showEmptyState = !isLoading && rows.length === 0;

  return (
    <div className="rounded-3xl border-soft bg-base shadow-lg backdrop-blur">
      <div className="overflow-hidden rounded-3xl">
        <table className="min-w-full border-collapse text-left text-sm text-secondary">
          <thead className="bg-surface text-xs uppercase tracking-wide text-muted">
            <tr>
              {columns.map((column) => (
                <th key={column.key.toString()} className="px-4 py-4 font-semibold">
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={columns.length} className="px-4 py-8 text-center text-muted">
                  Đang tải dữ liệu xe...
                </td>
              </tr>
            ) : showEmptyState ? (
              <tr>
                <td colSpan={columns.length} className="px-4 py-8 text-center text-muted">
                  Chưa có dữ liệu xe.
                </td>
              </tr>
            ) : (
              rows.map((row, index) => (
                <InventoryTableRow
                  key={row.id}
                  row={row}
                  columns={columns}
                  isEven={index % 2 === 0}
                />
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}


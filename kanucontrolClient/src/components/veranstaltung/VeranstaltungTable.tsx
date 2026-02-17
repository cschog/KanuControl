// src/components/veranstaltung/VeranstaltungTable.tsx
import * as React from "react";
import { GenericTable } from "@/components/common/GenericTable";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";
import { veranstaltungColumns } from "./veranstaltungColumns";

interface Props {
  data: VeranstaltungList[];
  total: number;

  page: number;
  pageSize: number;

  selectedId: number | null;

  onSelect: (row: VeranstaltungList | null) => void;

  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
}

export function VeranstaltungTable({
  data,
  total,
  page,
  pageSize,
  onSelect,
  onPageChange,
  onPageSizeChange,
}: Props) {
  return (
    <GenericTable<VeranstaltungList>
      rows={data}
      columns={veranstaltungColumns}
      /* ===== Single Select ===== */
      onSelectRow={(row) => onSelect(row ?? null)}
      /* ===== Server Paging ===== */
      paginationMode="server"
      rowCount={total}
      page={page}
      pageSize={pageSize}
      onPageChange={onPageChange}
      onPageSizeChange={onPageSizeChange}
      /* ===== Sorting ===== */
      initialSortField="beginnDatum"
    />
  );
}

// src/components/veranstaltung/VeranstaltungTable.tsx
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
  selectedId,
  onSelect,
  onPageChange,
  onPageSizeChange,
}: Props) {
  return (
    <GenericTable<VeranstaltungList>
      rows={data}
      columns={veranstaltungColumns}
      selectedRow={data.find((v) => v.id === selectedId) ?? null}
      onSelectRow={onSelect}
      paginationMode="server"
      rowCount={total}
      page={page}
      pageSize={pageSize}
      onPageChange={onPageChange}
      onPageSizeChange={onPageSizeChange}
      initialSortField="beginnDatum"
    />
  );
}

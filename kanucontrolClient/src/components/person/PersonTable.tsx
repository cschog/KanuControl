import React from "react";
import { GenericTable } from "@/components/common/GenericTable";
import { personColumns, PersonWithId } from "@/components/person/personColumns";
import { PersonList } from "@/api/types/Person";

interface PersonTableProps {
  data?: PersonList[];
  total: number;
  page: number;
  pageSize: number;

  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;

  selectedPersonId: number | null;
  onSelectPerson: (row: PersonList | null) => void;

  // ⭐ OPTIONAL machen
  sortField?: string;
  sortDirection?: "asc" | "desc";
  onSortChange?: (field: string, direction: "asc" | "desc") => void;
}

export const PersonTable: React.FC<PersonTableProps> = ({
  data = [],
  total,
  page,
  pageSize,
  onPageChange,
  onPageSizeChange,
  selectedPersonId,
  onSelectPerson,
  sortField,
  sortDirection,
  onSortChange,
}) => {
  const rows: PersonWithId[] = data.filter((p): p is PersonWithId => typeof p.id === "number");

  return (
    <GenericTable<PersonWithId>
      rows={rows}
      columns={personColumns}
      /* Single Select */
      selectedRowId={selectedPersonId}
      onSelectRow={onSelectPerson}
      /* Server Paging */
      paginationMode="server"
      rowCount={total}
      page={page}
      pageSize={pageSize}
      onPageChange={onPageChange}
      onPageSizeChange={onPageSizeChange}
      /* Server Sorting — KEIN Mapping mehr nötig */
      sortField={sortField}
      sortDirection={sortDirection}
      onSortChange={(field, dir) => {
        onSortChange?.(field, dir);
        onPageChange(0);
      }}
    />
  );
};

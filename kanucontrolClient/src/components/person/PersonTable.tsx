// PersonTable.tsx
import { GenericTable } from "@/components/common/GenericTable";
import { personColumns, PersonWithId } from "@/components/person/personColumns";
import { PersonList } from "@/api/types/Person";

interface PersonTableProps {
  data?: PersonList[]; // 👈 optional!
  total: number;
  page: number;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  selectedPersonId: number | null;
  onSelectPerson: (row: PersonList | null) => void;
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
}) => {
  const rows: PersonWithId[] = data.filter((p): p is PersonWithId => typeof p.id === "number");

  const selectedRow =
    selectedPersonId != null ? rows.find((r) => r.id === selectedPersonId) ?? null : null;

  const serverPaginationEnabled = typeof total === "number";

  return (
    <GenericTable<PersonWithId>
      rows={rows}
      columns={personColumns}
      selectedRow={selectedRow}
      onSelectRow={onSelectPerson}
      initialSortField="name"
      /* ✅ NUR setzen, wenn vollständig */
      paginationMode={serverPaginationEnabled ? "server" : undefined}
      rowCount={serverPaginationEnabled ? total : undefined}
      page={serverPaginationEnabled ? page : undefined}
      pageSize={serverPaginationEnabled ? pageSize : undefined}
      onPageChange={serverPaginationEnabled ? onPageChange : undefined}
      onPageSizeChange={serverPaginationEnabled ? onPageSizeChange : undefined}
    />
  );
};

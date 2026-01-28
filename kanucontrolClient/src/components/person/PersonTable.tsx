// PersonTable.tsx
import { GenericTable } from "@/components/common/GenericTable";
import { personColumns, PersonWithId } from "@/components/person/personColumns";
import { PersonList } from "@/api/types/Person";

interface PersonTableProps {
  data: PersonList[];
  selectedPersonId: number | null;
  onSelectPerson: (row: PersonList | null) => void;
}

export const PersonTable: React.FC<PersonTableProps> = ({
  data,
  selectedPersonId,
  onSelectPerson,
}) => {
  const rows: PersonWithId[] = data.filter((p): p is PersonWithId => typeof p.id === "number");

  const selectedRow =
    selectedPersonId != null ? rows.find((r) => r.id === selectedPersonId) ?? null : null;

  return (
    <GenericTable<PersonWithId>
      rows={rows}
      columns={personColumns}
      selectedRow={selectedRow}
      initialSortField="name"
      onSelectRow={onSelectPerson} // 🔑 bekommt PersonList
    />
  );
};

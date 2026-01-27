// PersonTable.tsx
import { GenericTable } from "@/components/common/GenericTable";
import { personColumns, PersonWithId } from "@/components/person/personColumns";
import { Person } from "@/api/types/Person";

interface PersonTableProps {
  data: Person[];
  selectedPerson: Person | null;
  onSelectPerson: (person: Person | null) => void;
}

export const PersonTable: React.FC<PersonTableProps> = ({
  data,
  selectedPerson,
  onSelectPerson,
}) => {
  const rows: PersonWithId[] = data.filter((p): p is PersonWithId => typeof p.id === "number");

  const selectedRow =
    selectedPerson && typeof selectedPerson.id === "number"
      ? rows.find((r) => r.id === selectedPerson.id) ?? null
      : null;


  return (
    <GenericTable<PersonWithId>
      rows={rows}
      columns={personColumns}
      selectedRow={selectedRow}
      initialSortField="name"
      onSelectRow={onSelectPerson}
    />
  );
};

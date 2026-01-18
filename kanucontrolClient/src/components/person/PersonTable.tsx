// PersonTable.tsx
import { Person } from "@/api/types/Person";
import { GenericTable } from "@/components/common/GenericTable";
import { personColumns } from "./personColumns";

interface PersonWithId extends Person {
  id: number;
}

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
  // ✅ Rows: nur Personen mit ID
  const rows: PersonWithId[] = data.filter(
    (p): p is PersonWithId => typeof p.id === "number"
  );

  // ✅ selectedRow: explizit als PersonWithId bestimmen
  const selectedRow: PersonWithId | null =
    selectedPerson && typeof selectedPerson.id === "number"
      ? rows.find((r) => r.id === selectedPerson.id) ?? null
      : null;

  return (
    <GenericTable<PersonWithId>
      rows={rows}
      columns={personColumns}
      selectedRow={selectedRow}
      initialSortModel={[
		{ field: "name", sort: "asc" },
		{ field: "vorname", sort: "asc" },
	  ]}
      onSelectRow={(row) => {
        // 🔁 Rückübersetzung: PersonWithId → Person
        onSelectPerson(row);
      }}
    />
  );
};
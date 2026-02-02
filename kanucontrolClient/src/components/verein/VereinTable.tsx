// components/verein/VereinTable.tsx
import  Verein  from "@/api/types/VereinFormModel";
import { GenericTable } from "@/components/common/GenericTable";
import { vereinColumns, VereinWithId } from "./vereinColumns";

interface VereinTableProps {
  data: Verein[];
  selectedVerein: Verein | null;
  onSelectVerein: (verein: Verein | null) => void;
}

export const VereinTable: React.FC<VereinTableProps> = ({
  data,
  selectedVerein,
  onSelectVerein,
}) => {
  // ✅ nur Vereine mit ID
  const rows: VereinWithId[] = data.filter((v): v is VereinWithId => typeof v.id === "number");

  // ✅ selektierte Zeile sicher ableiten
  const selectedRow: VereinWithId | null =
    selectedVerein && typeof selectedVerein.id === "number"
      ? rows.find((r) => r.id === selectedVerein.id) ?? null
      : null;

  return (
    <GenericTable<VereinWithId>
      rows={rows}
      columns={vereinColumns}
      selectedRow={selectedRow}
      onSelectRow={onSelectVerein}
      initialSortField="abk" // ✅ RICHTIG
    />
  );
};

import Verein from "@/api/types/VereinFormModel";
import { GenericTable } from "@/components/common/GenericTable";
import { vereinColumns, VereinWithId } from "./vereinColumns";

interface VereinTableProps {
  data: Verein[];
  selectedVerein: Verein | null;
  onSelectVerein: (verein: Verein | null) => void;
}

export const VereinTable: React.FC<VereinTableProps> = ({ data, onSelectVerein }) => {
  const rows: VereinWithId[] = data.filter((v): v is VereinWithId => typeof v.id === "number");

  return (
    <GenericTable<VereinWithId>
      rows={rows}
      columns={vereinColumns}
      onSelectRow={(row) => onSelectVerein(row ?? null)}
      initialSortField="abk"
    />
  );
};

import React from "react";
import { GenericTable } from "@/components/common/GenericTable";
import { personColumns, PersonWithId } from "@/components/person/personColumns";
import { PersonList } from "@/api/types/Person";

interface PersonTableProps {
  data?: PersonList[];

  /* Paging OPTIONAL */
  total?: number;
  page?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (size: number) => void;

  selectedPersonId: number | null;
  onSelectPerson: (row: PersonList | null) => void;

  sortField?: string;
  sortDirection?: "asc" | "desc";
  onSortChange?: (field: string, direction: "asc" | "desc") => void;
}

export const PersonTable: React.FC<PersonTableProps> = ({
  data = [],

  selectedPersonId,
  onSelectPerson,
}) => {
  const rows: PersonWithId[] = data.filter((p): p is PersonWithId => typeof p.id === "number");

  /* 🔑 Server Paging nur wenn alle Werte vorhanden */

 return (
   <GenericTable<PersonWithId>
     rows={rows}
     columns={personColumns}
     selectedRowId={selectedPersonId}
     onSelectRow={onSelectPerson}
   />
 );
};

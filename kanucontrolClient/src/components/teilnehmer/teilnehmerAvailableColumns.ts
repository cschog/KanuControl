import { ColumnDef } from "@tanstack/react-table";
import { PersonList } from "@/api/types/person/PersonList";

export const teilnehmerAvailableColumns: ColumnDef<PersonList>[] = [
  {
    id: "fullname",

    header: "Name",

    accessorFn: (row) => `${row.name}, ${row.vorname}`,

    sortingFn: "text",

    size: 260,
  },

  {
    accessorKey: "hauptvereinAbk",

    header: "Verein",

    size: 90,
  },
];

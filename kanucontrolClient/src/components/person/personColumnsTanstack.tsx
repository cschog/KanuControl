import { ColumnDef } from "@tanstack/react-table";

import { PersonList } from "@/api/types/Person";

export const personColumnsTanstack: ColumnDef<PersonList>[] = [
  {
    id: "fullName",
    header: "Name",
    accessorFn: (row) => `${row.name ?? ""}, ${row.vorname ?? ""}`,
  },

  {
    accessorKey: "alter",
    header: "Alter",
    cell: (info) => info.getValue<number | null>() ?? "-",
    enableSorting: false,
  },

  {
    accessorKey: "ort",
    header: "Ort",
    cell: (info) => info.getValue<string | null>() ?? "-",
    enableSorting: false,
  },

  {
    accessorKey: "hauptvereinAbk",
    header: "Verein",
    cell: (info) => info.getValue<string | null>() ?? "-",
    enableSorting: false,
  },
];

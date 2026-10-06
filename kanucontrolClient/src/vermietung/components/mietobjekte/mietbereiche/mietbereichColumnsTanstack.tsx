import type { ColumnDef } from "@tanstack/react-table";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";

export const mietbereichColumnsTanstack: ColumnDef<Mietbereich>[] = [
  {
    accessorKey: "bezeichnung",
    header: "Bezeichnung",
  },
  {
    accessorKey: "beschreibung",
    header: "Beschreibung",
  },
  {
    accessorKey: "mietbar",
    header: "Mietbar",
    cell: ({ row }) => (row.original.mietbar ? "Ja" : "Nein"),
  },
];

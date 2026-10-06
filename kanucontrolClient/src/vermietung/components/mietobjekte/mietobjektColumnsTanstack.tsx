import { ColumnDef } from "@tanstack/react-table";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

export const mietobjektColumnsTanstack: ColumnDef<Mietobjekt>[] = [
  {
    accessorKey: "aktiv",
    header: "",
    cell: ({ row }) => (row.original.aktiv ? "🟢" : "⚪"),
    size: 40,
  },

  {
    accessorKey: "bezeichnung",
    header: "Mietobjekt",
  },

  {
    accessorKey: "plz",
    header: "PLZ",
  },

  {
    accessorKey: "ort",
    header: "Ort",
  },

  {
    accessorKey: "mietbar",
    header: "Mietbar",
    cell: ({ row }) => (row.original.mietbar ? "Ja" : "Nein"),
  },
];

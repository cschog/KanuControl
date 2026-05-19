import { ColumnDef } from "@tanstack/react-table";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";

export const veranstaltungColumnsTanstack: ColumnDef<VeranstaltungList>[] = [
  {
    accessorKey: "aktiv",

    header: "",

    cell: ({ row }) => (row.original.aktiv ? "🟢" : "⚪"),

    size: 40,
  },

  {
    accessorKey: "name",

    header: "Veranstaltung",
  },

  {
    accessorKey: "typ",

    header: "Typ",
  },

  {
    accessorKey: "beginnDatum",

    header: "Beginn",
  },

  {
    accessorKey: "endeDatum",

    header: "Ende",
  },

  {
    accessorKey: "vereinAbk",

    header: "Verein",
  },

  {
    id: "leitung",

    header: "Leitung",

    cell: ({ row }) => {
      const r = row.original;

      if (!r.leiterName) {
        return "";
      }

      if (!r.leiterVorname) {
        return r.leiterName;
      }

      return `${r.leiterName}, ${r.leiterVorname}`;
    },
  },
];

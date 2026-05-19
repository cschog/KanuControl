import { ColumnDef } from "@tanstack/react-table";

import Verein from "@/api/types/VereinFormModel";

export interface VereinWithId extends Verein {
  id: number;
}

export const vereinColumnsTanstack: ColumnDef<VereinWithId>[] = [
  {
    accessorKey: "abk",
    header: "Abk.",
    enableSorting: true,
  },

  {
    accessorKey: "name",
    header: "Verein",
    enableSorting: true,
  },

  {
    accessorKey: "mitgliederCount",
    header: "Mitgl.",
    enableSorting: true,

    cell: ({ getValue }) => {
      const value = getValue<number>();
      return value && value > 0 ? value : "";
    },

    meta: {
      align: "center",
    },
  },

  {
    accessorKey: "strasse",
    header: "Straße",
    enableSorting: true,
  },

  {
    accessorKey: "plz",
    header: "PLZ",
    enableSorting: true,
  },

  {
    accessorKey: "ort",
    header: "Ort",
    enableSorting: true,
  },
];

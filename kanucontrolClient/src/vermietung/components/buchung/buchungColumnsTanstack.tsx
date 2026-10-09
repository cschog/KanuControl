import type { ColumnDef } from "@tanstack/react-table";

import type { Buchung } from "@/vermietung/types/Buchung";
import { formatGermanDate } from "@/core/utils/dateUtils";

export const buchungColumnsTanstack: ColumnDef<Buchung>[] = [
  {
    accessorKey: "buchungsnummer",
    header: "Buchungsnummer",
  },
  {
    accessorKey: "mieterName",
    header: "Mieter",
    cell: ({ row }) => `${row.original.mieterVorname} ${row.original.mieterName}`,
  },
  {
    accessorKey: "mietobjektBezeichnung",
    header: "Mietobjekt",
  },
  {
    accessorKey: "anreise",
    header: "Beginn",
    cell: ({ row }) => formatGermanDate(row.original.anreise),
  },
  {
    accessorKey: "abreise",
    header: "Ende",
    cell: ({ row }) =>
      row.original.unbefristet ? "Unbefristet" : formatGermanDate(row.original.abreise),
  },
  {
    accessorKey: "status",
    header: "Status",
    cell: ({ row }) => formatStatus(row.original.status),
  },
];

function formatStatus(status: Buchung["status"]): string {
  switch (status) {
    case "ANFRAGE":
      return "Anfrage";

    case "BESTAETIGT":
      return "Bestätigt";

    case "STORNIERT":
      return "Storniert";

    case "ABGESCHLOSSEN":
      return "Abgeschlossen";

    default:
      return status;
  }
}

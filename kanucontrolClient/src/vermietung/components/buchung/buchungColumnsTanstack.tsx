import type { ColumnDef } from "@tanstack/react-table";

import type { Buchung } from "@/vermietung/types/Buchung";
import { formatGermanDate } from "@/core/utils/dateUtils";

const textVergleich = new Intl.Collator("de", { numeric: true, sensitivity: "base" });

export const buchungColumnsTanstack: ColumnDef<Buchung>[] = [
  {
    accessorKey: "buchungsnummer",
    header: "Buchungsnummer",
    sortingFn: (a, b) => textVergleich.compare(a.original.buchungsnummer ?? "", b.original.buchungsnummer ?? "")
      || (a.original.buchungsnummer ?? "").localeCompare(b.original.buchungsnummer ?? "", "de"),
  },
  {
    accessorKey: "mieterName",
    header: "Mieter",
    sortingFn: (a, b) => textVergleich.compare(a.original.mieterName ?? "", b.original.mieterName ?? "")
      || textVergleich.compare(a.original.mieterVorname ?? "", b.original.mieterVorname ?? ""),
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

    case "ABGELEHNT":
      return "Abgelehnt";

    case "RESERVIERT":
      return "Reserviert";

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

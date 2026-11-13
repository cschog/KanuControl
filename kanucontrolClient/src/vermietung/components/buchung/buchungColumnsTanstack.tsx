import { formatPersonName } from "@/core/utils/personName";
import type { ColumnDef } from "@tanstack/react-table";

import type { Buchung } from "@/vermietung/types/Buchung";
import { formatGermanDate } from "@/core/utils/dateUtils";

const textVergleich = new Intl.Collator("de", { numeric: true, sensitivity: "base" });

export const buchungColumnsTanstack = (showMietobjekt: boolean): ColumnDef<Buchung>[] => [
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
    cell: ({ row }) => `${formatPersonName(row.original.mieterName, row.original.mieterVorname, "first")}`,
  },
  ...(showMietobjekt ? [{
    accessorKey: "mietobjektBezeichnung",
    header: "Mietobjekt",
  }] : [{
    id: "veranstalter",
    accessorFn: (buchung: Buchung) => buchung.veranstalter?.name ?? "",
    header: "Veranstalter",
    sortingFn: (a, b) => textVergleich.compare(a.original.veranstalter?.name ?? "", b.original.veranstalter?.name ?? ""),
    cell: ({ row }) => row.original.veranstalter?.name ?? "—",
  }] satisfies ColumnDef<Buchung>[]),
  {
    accessorKey: "veranstaltung",
    header: "Veranstaltung",
    enableSorting: true,
    sortingFn: (a, b) => textVergleich.compare(a.original.veranstaltung ?? "", b.original.veranstaltung ?? ""),
    cell: ({ row }) => row.original.veranstaltung ?? "",
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
